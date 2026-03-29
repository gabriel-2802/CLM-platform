package clm.demo.services;

import clm.demo.dto.requests.FieldMappingRequest;
import clm.demo.dto.requests.UploadTemplateRequest;
import clm.demo.dto.requests.UpdateFieldLabelRequest;
import clm.demo.dto.responses.*;
import clm.demo.exceptions.EmptyFileNameException;
import clm.demo.exceptions.ResourceNotFoundException;
import clm.demo.exceptions.UnsupportedFileException;
import clm.demo.mappers.ContractTemplateMapper;
import clm.demo.mappers.ParsedDocumentMapper;
import clm.demo.models.ContractTemplate;
import clm.demo.models.TemplateField;
import clm.demo.models.enums.DataType;
import clm.demo.models.enums.DocumentFormat;

import clm.demo.repositories.ContractTemplateRepository;
import clm.demo.repositories.TemplateFieldRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing contract templates.
 * Handles template upload, parsing, field extraction, and mapping operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TemplateService {

    private final ContractTemplateRepository templateRepository;
    private final TemplateFieldRepository templateFieldRepository;
    private final FileParserService fileParserService;
    private final ContractTemplateMapper contractTemplateMapper;
    private final ParsedDocumentMapper parsedDocumentMapper;
    private final FileZipService zipService;

    /**
     * Generates (uploads and parses) a new contract template from an uploaded file.
     * Extracts placeholders and creates TemplateField entities.
     *
     * @param request containing file, template name, and description
     * @return ParsedTemplateResponseDTO with parsed content and template info
     * @throws IOException if file parsing fails
     * @throws IllegalArgumentException if file is empty
     */
    public ParsedTemplateResponseDTO uploadTemplate(UploadTemplateRequest request) throws IOException {
        log.info("Generating template: {}", request.getTemplateName());

        // validation
        if (request.getFile().isEmpty()) {
            log.warn("Empty file provided for template generation");
            throw new IllegalArgumentException("File cannot be empty");
        }

        // parse the uploaded file
        DocumentFormat format = detectDocumentFormat(request.getFile());
        FileParserService.ParsedDocumentResponse parsedDoc = fileParserService.parseTemplate(request.getFile(), format);

        // create and save the ContractTemplate entity
        ContractTemplate template = ContractTemplate.builder()
                .templateName(request.getTemplateName())
                .description(request.getDescription())
                .documentFormat(format)
                .documentContent(zipService.compress(request.getFile().getBytes()))
                .fieldCount(parsedDoc.getPlaceholderCount())
                .build();

        template = templateRepository.save(template);
        log.info("Template saved with ID: {}", template.getId());

        // create TemplateField entities for each placeholder
        final ContractTemplate finalTemplate = template;
        List<TemplateField> fields = parsedDoc.getPlaceholders().stream()
                .map(placeholder -> TemplateField.builder()
                        .contractTemplate(finalTemplate)
                        .fieldLabel("Field " + (placeholder.getPosition() + 1))
                        .fieldPosition(placeholder.getPosition())
                        .placeholderText(placeholder.getPlaceholderText())
                        .build())
                .collect(Collectors.toList());

        templateFieldRepository.saveAll(fields);
        log.info("Created {} template fields", fields.size());

        // map parsed document to response DTO using mapper
        ParsedTemplateResponseDTO response = parsedDocumentMapper.toResponseDTO(parsedDoc);
        response.setTemplateId(template.getId());
        response.setTemplateName(template.getTemplateName());

        return response;
    }

    /**
     * Updates the label for a specific field in a template.
     * Called by Client Management Service to set display labels.
     *
     * @param templateId the template ID
     * @param fieldId the field ID
     * @param request containing the new label
     * @return TemplateFieldResponseDTO with updated field
     * @throws ResourceNotFoundException if template or field not found
     */
    public TemplateFieldResponseDTO updateFieldLabel(Long templateId, Long fieldId, UpdateFieldLabelRequest request) {
        log.info("Updating field label: template={}, field={}, label={}", templateId, fieldId, request.getFieldLabel());
        
        ContractTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + templateId));
        
        TemplateField field = templateFieldRepository.findById(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field not found: " + fieldId));
        
        // Verify field belongs to the template
        if (!field.getContractTemplate().getId().equals(templateId)) {
            throw new RuntimeException("Field does not belong to this template");
        }
        
        field.setFieldLabel(request.getFieldLabel());
        field = templateFieldRepository.save(field);
        
        log.info("Field label updated: {}", fieldId);
        return new TemplateFieldResponseDTO(field);
    }

    /**
     * Retrieves a template by ID with all its fields.
     *
     * @param templateId the template ID
     * @return TemplateResponseDTO containing template details
     * @throws RuntimeException if template not found
     */
    @Transactional(readOnly = true)
    public TemplateResponseDTO getTemplate(Long templateId) {
        log.info("Fetching template: {}", templateId);
        return templateRepository.findById(templateId)
                .map(contractTemplateMapper::toResponseDTO)
                .orElseThrow(() -> {
                    log.warn("Template not found: {}", templateId);
                    return new RuntimeException("Template not found: " + templateId);
                });
    }

    /**
     * Retrieves all available templates with their metadata.
     * Returns a list of all templates sorted by creation date (newest first).
     *
     * @return List of TemplateResponseDTO containing all templates
     */
    @Transactional(readOnly = true)
    public List<TemplateResponseDTO> getAllTemplates() {
        log.info("Fetching all templates");
        return templateRepository.findAll()
                .stream()
                .map(contractTemplateMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Deletes a template by ID along with all associated fields and mappings.
     *
     * @param templateId the template ID
     * @throws RuntimeException if template not found
     */
    public void deleteTemplate(Long templateId) {

        if (!templateRepository.existsById(templateId)) {
            log.warn("Template not found for deletion: {}", templateId);
            throw new ResourceNotFoundException("Template not found: " + templateId);
        }

        templateRepository.deleteById(templateId);
    }

    /**
     * Helper method to detect document format from file extension.
     */
    private DocumentFormat detectDocumentFormat(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new EmptyFileNameException("File must have a valid filename");
        }

        if (filename.toLowerCase().endsWith(".pdf")) {
            return DocumentFormat.PDF;
        } else if (filename.toLowerCase().endsWith(".docx")) {
            return DocumentFormat.DOCX;
        } else {
            throw new UnsupportedFileException();
        }
    }
    
    
    /**
     * Batch updates field mappings for a template.
     * Allows setting labels, data types, required status, and format patterns for multiple fields at once.
     *
     * @param request containing templateId and list of field mapping definitions
     * @return List of TemplateFieldResponseDTO with all updated fields
     * @throws ResourceNotFoundException if template or any field not found
     */
    public List<TemplateFieldResponseDTO> updateFieldLabels(FieldMappingRequest request) {

        ContractTemplate template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + request.getTemplateId()));

        // update each field with the provided label
        List<TemplateField> updatedFields = new java.util.ArrayList<>();
        for (FieldMappingRequest.FieldMappingDefinition mapping : request.getMappings()) {
            TemplateField field = templateFieldRepository.findById(mapping.getFieldId())
                    .orElseThrow(() -> new ResourceNotFoundException("Field not found: " + mapping.getFieldId()));

            // verify field belongs to the template
            if (!field.getContractTemplate().getId().equals(request.getTemplateId())) {
                log.warn("Field {} does not belong to template {}", mapping.getFieldId(), request.getTemplateId());
                throw new RuntimeException("Field does not belong to this template");
            }

            // update field properties
            field.setFieldLabel(mapping.getFieldLabel());
            field.setDataType(convertStringToDataType(mapping.getDataType()));
            field.setIsRequired(mapping.isRequired());
            field.setFormatPattern(mapping.getFormatPattern());

            TemplateField savedField = templateFieldRepository.save(field);
            updatedFields.add(savedField);
            log.debug("Field updated: {} with label: {}", mapping.getFieldId(), mapping.getFieldLabel());
        }

        log.info("Successfully updated {} fields for template: {}", request.getMappings().size(), request.getTemplateId());

        // return the list of updated fields as response DTOs
        return updatedFields.stream()
                .map(TemplateFieldResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to convert string data type to enum.
     */
    private DataType convertStringToDataType(String dataTypeStr) {
        try {
            return DataType.valueOf(dataTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid data type: {}, defaulting to STRING", dataTypeStr);
            return DataType.STRING;
        }
    }
}

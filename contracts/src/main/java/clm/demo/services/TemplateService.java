package clm.demo.services;

import clm.demo.dto.requests.FieldMappingRequest;
import clm.demo.dto.requests.UploadTemplateRequest;
import clm.demo.dto.responses.*;
import clm.demo.exceptions.ResourceNotFoundException;
import clm.demo.exceptions.TemplateFieldOwnershipException;
import clm.demo.exceptions.TemplateUploadException;
import clm.demo.exceptions.DuplicateTemplateNameException;
import clm.demo.mappers.ContractTemplateMapper;
import clm.demo.models.Template;
import clm.demo.models.TemplateField;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.repositories.TemplateRepository;
import clm.demo.repositories.TemplateFieldRepository;
import clm.demo.utils.docx.DocxNormalizer;
import clm.demo.utils.file.FileParser;
import clm.demo.utils.file.FileUtils;
import clm.demo.utils.file.PlaceholderProcessor;
import clm.demo.utils.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Service for managing contract templates.
 * Handles template upload, parsing, field extraction, and mapping operations.
 */

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final TemplateFieldRepository templateFieldRepository;

    private final ContractTemplateMapper contractTemplateMapper;

    /**
     * Uploads and parses a new contract template from an uploaded file.
     * Extracts placeholders and creates TemplateField entities.
     *
     * @param request containing file, template name, and description
     * @return TemplateUploadResponseDTO with template info and document text with placeholders replaced by field IDs
     * @throws IllegalArgumentException if file is empty
     * @throws DuplicateTemplateNameException if a template with the same name already exists
     */
    @Transactional
    public TemplateUploadResponseDTO uploadTemplate(UploadTemplateRequest request) {

        if (request.getFile().isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // check if a template with the same name already exists
        if (templateRepository.findByTemplateName(request.getTemplateName()).isPresent()) {
            throw new DuplicateTemplateNameException(
                    "Template with name '" + request.getTemplateName() + "' already exists"
            );
        }

        try {
            // read file bytes once
            byte[] fileBytes = request.getFile().getBytes();

            DocumentFormat uploadedFormat = Utils.detectDocumentFormat(fileBytes);
            FileParser.ParsedDocument parsedDoc = FileParser.parseTemplate(request.getFile(), uploadedFormat);

            // convert to DOCX if uploaded as PDF
            byte[] docxBytes = uploadedFormat == DocumentFormat.PDF
                    ? FileUtils.convert(fileBytes, DocumentFormat.PDF, DocumentFormat.DOCX)
                    : fileBytes;

            // normalize every placeholder to exactly 4 dots before storing
            docxBytes = DocxNormalizer.normalizePlaceholdersInDocx(docxBytes);

            Template template = templateRepository.save(
                    Template.builder()
                            .templateName(request.getTemplateName())
                            .description(request.getDescription())
                            .documentFormat(DocumentFormat.DOCX)
                            .documentContent(FileUtils.compress(docxBytes))
                            .fieldCount(parsedDoc.placeholderCount())
                            .build()
            );

            // create TemplateField entities for each placeholder
            List<TemplateField> savedFields = templateFieldRepository.saveAll(
                    IntStream.range(0, parsedDoc.placeholderCount())
                            .mapToObj(position -> TemplateField.builder()
                                    .contractTemplate(template)
                                    .fieldPosition(position)
                                    .build())
                            .toList()
            );

            // simple positional replace
            String documentText = replaceWithFieldIds(parsedDoc.documentText(), savedFields);

            return TemplateUploadResponseDTO.builder()
                    .templateId(template.getId())
                    .templateName(template.getTemplateName())
                    .documentText(documentText)
                    .build();

        } catch (IOException e) {
            throw new TemplateUploadException("Failed to process uploaded template: " + e.getMessage());
        }
    }

    /**
     * Replaces each dot-sequence placeholder in the normalized document text
     * with a {{fieldId}} token, matched positionally to savedFields.
     * Safe because the text is already normalized : all placeholders are exactly 4 dots.
     *
     * @param normalizedText the already-normalized document text
     * @param savedFields list of TemplateField entities with assigned IDs, in position order
     * @return document text with placeholders replaced by {{fieldId}}
     */
    private String replaceWithFieldIds(String normalizedText, List<TemplateField> savedFields) {
        return PlaceholderProcessor.substituteEach(
                normalizedText,
                i -> i < savedFields.size() ? "{{" + savedFields.get(i).getId() + "}}" : null
        ).text();
    }

    /**
     * Retrieves a template by ID with all its fields.
     *
     * @param templateId the template ID
     * @return TemplateResponseDTO containing template details
     * @throws ResourceNotFoundException if template not found
     */
    @Transactional(readOnly = true)
    public TemplateResponseDTO getTemplate(Long templateId) {
        return templateRepository.findById(templateId)
                .map(contractTemplateMapper::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + templateId));
    }

    /**
     * Retrieves all templates with pagination, sorted by creation date descending.
     *
     * @param page zero-based page index
     * @param size number of records per page
     * @return page of TemplateResponseDTOs
     */
    @Transactional(readOnly = true)
    public Page<TemplateResponseDTO> getAllTemplates(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return templateRepository.findAll(pageable)
                .map(contractTemplateMapper::toResponseDTO);
    }

    /**
     * Deletes a template by ID along with all associated fields and mappings.
     *
     * @param templateId the template ID
     * @throws ResourceNotFoundException if template not found
     */
    @Transactional
    public void deleteTemplate(Long templateId) {
        if (!templateRepository.existsById(templateId)) {
            throw new ResourceNotFoundException("Template not found: " + templateId);
        }
        templateRepository.deleteById(templateId);
    }

    /**
     * Batch updates field mappings for a template.
     * Allows setting labels, data types, required status, and format patterns for multiple fields at once.
     *
     * @param request containing templateId and list of field mapping definitions
     * @return list of TemplateFieldResponseDTO with all updated fields
     * @throws ResourceNotFoundException       if template or any field not found
     * @throws TemplateFieldOwnershipException if a field does not belong to the template
     */
    @Transactional
    public List<TemplateFieldResponseDTO> updateFieldLabels(FieldMappingRequest request) {
        if (!templateRepository.existsById(request.getTemplateId())) {
            throw new ResourceNotFoundException("Template not found: " + request.getTemplateId());
        }

        // fetch all fields in one query instead of N individual lookups
        Set<Long> fieldIds = request.getMappings().stream()
                .map(FieldMappingRequest.FieldMappingDefinition::getFieldId)
                .collect(Collectors.toSet());

        Map<Long, TemplateField> fieldMap = templateFieldRepository.findAllById(fieldIds)
                .stream()
                .collect(Collectors.toMap(TemplateField::getId, f -> f));

        List<TemplateField> updatedFields = request.getMappings().stream()
                .map(mapping -> {
                    TemplateField field = fieldMap.get(mapping.getFieldId());
                    if (field == null) {
                        throw new ResourceNotFoundException("Field not found: " + mapping.getFieldId());
                    }
                    if (!field.getContractTemplate().getId().equals(request.getTemplateId())) {
                        throw new TemplateFieldOwnershipException(
                                "Field " + mapping.getFieldId() + " does not belong to template " + request.getTemplateId()
                        );
                    }
                    field.setFieldLabel(mapping.getFieldLabel());
                    field.setDataType(Utils.convertStringToDataType(mapping.getDataType()));
                    field.setIsRequired(mapping.isRequired());
                    field.setFormatPattern(mapping.getFormatPattern());
                    return field;
                })
                .toList();

        List<TemplateField> savedFields = templateFieldRepository.saveAll(updatedFields);

        return savedFields.stream()
                .map(TemplateFieldResponseDTO::new)
                .toList();
    }
}
package clm.demo.services;

import clm.demo.dto.requests.UploadTemplateRequest;
import clm.demo.dto.requests.FieldMappingRequest;
import clm.demo.dto.responses.*;
import clm.demo.exceptions.EmptyFileNameException;
import clm.demo.exceptions.ResourceNotFoundException;
import clm.demo.exceptions.UnsupportedFileException;
import clm.demo.mappers.ContractTemplateMapper;
import clm.demo.mappers.ParsedDocumentMapper;
import clm.demo.models.ContractTemplate;
import clm.demo.models.TemplateField;
import clm.demo.models.enums.DocumentFormat;

import clm.demo.repositories.ContractTemplateRepository;
import clm.demo.repositories.TemplateFieldRepository;
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
                .isFullyMapped(false)
                .build();

        template = templateRepository.save(template);
        log.info("Template saved with ID: {}", template.getId());

        // create TemplateField entities for each placeholder
        final ContractTemplate finalTemplate = template;
        List<TemplateField> fields = parsedDoc.getPlaceholders().stream()
                .map(placeholder -> TemplateField.builder()
                        .contractTemplate(finalTemplate)
                        .fieldName("field_" + placeholder.getPosition())
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
     * Updates multiple field mappings for a template in a batch operation.
     * Maps all provided template fields to their respective database columns.
     *
     * @param request containing template ID and list of field mapping definitions
     * @return BatchFieldMappingResponseDTO with status for each mapping
     */
    public BatchFieldMappingResponseDTO updateFieldMappings(FieldMappingRequest request) {
        // Implementation will be added
        return null;
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
}

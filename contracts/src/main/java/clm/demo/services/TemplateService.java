package clm.demo.services;

import clm.demo.dto.requests.FieldMappingRequest;
import clm.demo.dto.requests.UploadTemplateRequest;
import clm.demo.dto.responses.*;
import clm.demo.exceptions.ResourceNotFoundException;
import clm.demo.exceptions.TemplateDownloadException;
import clm.demo.exceptions.TemplateFieldOwnershipException;
import clm.demo.exceptions.TemplateUploadException;
import clm.demo.mappers.ContractTemplateMapper;
import clm.demo.mappers.ParsedDocumentMapper;
import clm.demo.models.Template;
import clm.demo.models.TemplateField;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.repositories.TemplateRepository;
import clm.demo.repositories.TemplateFieldRepository;
import clm.demo.services.file.actions.FileContentReplacementService;
import clm.demo.services.file.actions.FileConverterService;
import clm.demo.services.file.actions.FileParserService;
import clm.demo.utils.ZipUtils;
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
    private final ParsedDocumentMapper parsedDocumentMapper;

    private final FileParserService fileParserService;
    private final FileConverterService fileConverterService;
    private final FileContentReplacementService fileContentReplacementService;

    /**
     * Uploads and parses a new contract template from an uploaded file.
     * Extracts placeholders and creates TemplateField entities.
     *
     * @param request containing file, template name, and description
     * @return ParsedTemplateResponseDTO with parsed content and template info
     * @throws IllegalArgumentException if file is empty
     */
    @Transactional
    public ParsedTemplateResponseDTO uploadTemplate(UploadTemplateRequest request) {

        if (request.getFile().isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        try {
            DocumentFormat uploadedFormat = Utils.detectDocumentFormat(request.getFile().getBytes());
            FileParserService.ParsedDocumentResponse parsedDoc = fileParserService.parseTemplate(request.getFile(), uploadedFormat);

            // templates are always stored as DOCX,  convert if the upload was a PDF
            byte[] docxBytes = request.getFile().getBytes();
            if (uploadedFormat == DocumentFormat.PDF) {
                docxBytes = fileConverterService.convert(docxBytes, DocumentFormat.PDF, DocumentFormat.DOCX);
            }

            // normalize every placeholder to exactly 4 dots before storing
            docxBytes = fileContentReplacementService.normalizePlaceholdersInDocx(docxBytes);

            Template template = templateRepository.save(
                    Template.builder()
                            .templateName(request.getTemplateName())
                            .description(request.getDescription())
                            .documentFormat(DocumentFormat.DOCX)
                            .documentContent(ZipUtils.compress(docxBytes))
                            .fieldCount(parsedDoc.getPlaceholderCount())
                            .build()
            );

            // create TemplateField entities for each placeholder
            List<TemplateField> fields = parsedDoc.getPlaceholders().stream()
                    .map(placeholder -> TemplateField.builder()
                            .contractTemplate(template)
                            .fieldPosition(placeholder.getPosition())
                            .placeholderText(placeholder.getPlaceholderText())
                            .build())
                    .toList();

            List<TemplateField> savedFields = templateFieldRepository.saveAll(fields);

            // set field IDs on placeholders for client-side reference
            for (int i = 0; i < parsedDoc.getPlaceholders().size(); i++) {
                parsedDoc.getPlaceholders().get(i).setFieldId(savedFields.get(i).getId());
            }

            ParsedTemplateResponseDTO response = parsedDocumentMapper.toResponseDTO(parsedDoc);
            response.setTemplateId(template.getId());
            response.setTemplateName(template.getTemplateName());

            return response;

        } catch (IOException e) {
            throw new TemplateUploadException("Failed to process uploaded template: " + e.getMessage());
        }
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
     * Downloads a template in the requested format.
     * Decompresses stored content and converts if necessary.
     *
     * @param templateId     the template ID to download
     * @param targetFormat   the desired output format
     * @return document bytes in the requested format
     * @throws ResourceNotFoundException if template not found
     */
    @Transactional(readOnly = true)
    public byte[] downloadTemplate(Long templateId, DocumentFormat targetFormat) {
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + templateId));

        try {
            byte[] decompressedContent = ZipUtils.decompress(template.getDocumentContent());

            if (template.getDocumentFormat() == targetFormat) {
                return decompressedContent;
            }

            return fileConverterService.convert(decompressedContent, template.getDocumentFormat(), targetFormat);

        } catch (IOException e) {
            throw new TemplateDownloadException("Failed to download template " + templateId + ": " + e.getMessage());
        }
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
package clm.demo.services;

import clm.demo.cache.CacheNames;
import clm.demo.dto.requests.FieldMappingRequest;
import clm.demo.dto.requests.UploadTemplateRequest;
import clm.demo.dto.responses.TemplateFieldResponseDTO;
import clm.demo.dto.responses.TemplateResponseDTO;
import clm.demo.dto.responses.TemplateUploadResponseDTO;
import clm.demo.exceptions.exceptions.DuplicateTemplateNameException;
import clm.demo.exceptions.exceptions.ResourceNotFoundException;
import clm.demo.exceptions.exceptions.TemplateFieldOwnershipException;
import clm.demo.exceptions.exceptions.TemplateUploadException;
import clm.demo.mappers.DocumentTemplateMapper;
import clm.demo.models.DocumentTemplate;
import clm.demo.models.TemplateField;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.repositories.DocumentTemplateRepository;
import clm.demo.repositories.TemplateFieldRepository;
import clm.demo.utils.Utils;
import clm.demo.utils.docx.DocxNormalizer;
import clm.demo.utils.file.FileParser;
import clm.demo.utils.file.FileUtils;
import clm.demo.utils.file.PlaceholderProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateService {

    private static final String SORT_FIELD_CREATED_AT = "createdAt";

    private final DocumentTemplateRepository templateRepository;
    private final TemplateFieldRepository    templateFieldRepository;
    private final DocumentTemplateMapper     templateMapper;
    private final FileUtils                  fileUtils;

    @Transactional
    public TemplateUploadResponseDTO uploadTemplate(UploadTemplateRequest request) {
        if (request.getFile().isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (templateRepository.findByTemplateName(request.getTemplateName()).isPresent()) {
            throw new DuplicateTemplateNameException(
                    "Template with name '" + request.getTemplateName() + "' already exists");
        }

        try {
            byte[] fileBytes      = request.getFile().getBytes();
            DocumentFormat format = Utils.detectDocumentFormat(fileBytes);

            FileParser.ParsedDocument parsedDoc = FileParser.parseTemplate(request.getFile(), format);

            byte[] docxBytes = format == DocumentFormat.PDF
                    ? fileUtils.convert(fileBytes, DocumentFormat.PDF, DocumentFormat.DOCX)
                    : fileBytes;

            docxBytes = DocxNormalizer.normalizePlaceholdersInDocx(docxBytes);

            DocumentTemplate template = templateRepository.save(
                    DocumentTemplate.builder()
                            .templateName(request.getTemplateName())
                            .description(request.getDescription())
                            .documentFormat(DocumentFormat.DOCX)
                            .documentContent(fileUtils.compress(docxBytes))
                            .fieldCount(parsedDoc.placeholderCount())
                            .build()
            );

            List<TemplateField> savedFields = templateFieldRepository.saveAll(
                    IntStream.range(0, parsedDoc.placeholderCount())
                            .mapToObj(position -> TemplateField.builder()
                                    .documentTemplate(template)
                                    .fieldPosition(position)
                                    .build())
                            .toList()
            );

            log.info("Template '{}' uploaded: {} placeholder(s) extracted",
                    template.getTemplateName(), savedFields.size());

            return TemplateUploadResponseDTO.builder()
                    .templateId(template.getId())
                    .templateName(template.getTemplateName())
                    .documentText(replaceWithFieldIds(parsedDoc.documentText(), savedFields))
                    .build();

        } catch (IOException e) {
            throw new TemplateUploadException("Failed to process uploaded template: " + e.getMessage());
        }
    }

    @Cacheable(value = CacheNames.TEMPLATES, key = "#templateId")
    @Transactional(readOnly = true)
    public TemplateResponseDTO getTemplate(Long templateId) {
        return templateRepository.findById(templateId)
                .map(templateMapper::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + templateId));
    }

    @Transactional(readOnly = true)
    public Page<TemplateResponseDTO> getAllTemplates(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, SORT_FIELD_CREATED_AT));
        return templateRepository.findAll(pageable)
                .map(templateMapper::toResponseDTO);
    }

    @CacheEvict(value = CacheNames.TEMPLATES, key = "#templateId")
    @Transactional
    public void deleteTemplate(Long templateId) {
        if (!templateRepository.existsById(templateId)) {
            throw new ResourceNotFoundException("Template not found: " + templateId);
        }
        templateRepository.deleteById(templateId);
        log.info("Template {} deleted", templateId);
    }

    @CacheEvict(value = CacheNames.TEMPLATES, key = "#request.templateId")
    @Transactional
    public List<TemplateFieldResponseDTO> updateFieldLabels(FieldMappingRequest request) {
        if (!templateRepository.existsById(request.getTemplateId())) {
            throw new ResourceNotFoundException("Template not found: " + request.getTemplateId());
        }

        Set<Long> fieldIds = request.getMappings().stream()
                .map(FieldMappingRequest.FieldMappingDefinition::getFieldId)
                .collect(Collectors.toSet());

        Map<Long, TemplateField> fieldMap = templateFieldRepository.findAllById(fieldIds)
                .stream()
                .collect(Collectors.toMap(TemplateField::getId, f -> f));

        List<TemplateField> updatedFields = request.getMappings().stream()
                .map(mapping -> resolveAndUpdateField(mapping, fieldMap, request.getTemplateId()))
                .toList();

        List<TemplateField> savedFields = templateFieldRepository.saveAll(updatedFields);

        DocumentTemplate template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Template not found: " + request.getTemplateId()));

        updateMappingStatus(template);

        return savedFields.stream()
                .map(TemplateFieldResponseDTO::new)
                .toList();
    }

    private TemplateField resolveAndUpdateField(
            FieldMappingRequest.FieldMappingDefinition mapping,
            Map<Long, TemplateField> fieldMap,
            Long templateId) {

        TemplateField field = fieldMap.get(mapping.getFieldId());

        if (Objects.isNull(field)) {
            throw new ResourceNotFoundException("Field not found: " + mapping.getFieldId());
        }
        if (!field.getDocumentTemplate().getId().equals(templateId)) {
            throw new TemplateFieldOwnershipException(
                    "Field " + mapping.getFieldId() + " does not belong to template " + templateId);
        }

        field.setFieldLabel(mapping.getFieldLabel());
        field.setDataType(Utils.convertStringToDataType(mapping.getDataType()));
        field.setIsRequired(mapping.isRequired());
        field.setFormatPattern(mapping.getFormatPattern());
        return field;
    }

    private void updateMappingStatus(DocumentTemplate template) {
        long mappedCount = template.getTemplateFields().stream()
                .filter(f -> Objects.nonNull(f.getFieldLabel()) && !f.getFieldLabel().isBlank())
                .count();

        boolean fullyMapped = !template.getTemplateFields().isEmpty()
                && mappedCount == template.getFieldCount();

        template.setIsFullyMapped(fullyMapped);
        templateRepository.save(template);

        log.info("Template {} field labels updated: {}/{} fields mapped",
                template.getId(), mappedCount, template.getFieldCount());
    }

    private String replaceWithFieldIds(String normalizedText, List<TemplateField> savedFields) {
        return PlaceholderProcessor.substituteEach(
                normalizedText,
                i -> i < savedFields.size() ? "{{" + savedFields.get(i).getId() + "}}" : null
        ).text();
    }
}
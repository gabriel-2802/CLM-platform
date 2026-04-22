package clm.demo.services;

import clm.demo.dto.requests.FieldMappingRequest;
import clm.demo.dto.requests.UploadTemplateRequest;
import clm.demo.dto.responses.*;
import clm.demo.exceptions.ResourceNotFoundException;
import clm.demo.exceptions.TemplateFieldOwnershipException;
import clm.demo.exceptions.TemplateUploadException;
import clm.demo.exceptions.DuplicateTemplateNameException;
import clm.demo.mappers.DocumentTemplateMapper;
import clm.demo.models.DocumentTemplate;
import clm.demo.models.TemplateField;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.repositories.DocumentTemplateRepository;
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

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final DocumentTemplateRepository templateRepository;
    private final TemplateFieldRepository templateFieldRepository;
    private final DocumentTemplateMapper templateMapper;
    private final FileUtils fileUtils;

    @Transactional
    public TemplateUploadResponseDTO uploadTemplate(UploadTemplateRequest request) {
        if (request.getFile().isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (templateRepository.findByTemplateName(request.getTemplateName()).isPresent()) {
            throw new DuplicateTemplateNameException(
                    "Template with name '" + request.getTemplateName() + "' already exists"
            );
        }

        try {
            byte[] fileBytes = request.getFile().getBytes();
            DocumentFormat uploadedFormat = Utils.detectDocumentFormat(fileBytes);
            FileParser.ParsedDocument parsedDoc = FileParser.parseTemplate(request.getFile(), uploadedFormat);

            byte[] docxBytes = uploadedFormat == DocumentFormat.PDF
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

    private String replaceWithFieldIds(String normalizedText, List<TemplateField> savedFields) {
        return PlaceholderProcessor.substituteEach(
                normalizedText,
                i -> i < savedFields.size() ? "{{" + savedFields.get(i).getId() + "}}" : null
        ).text();
    }

    @Transactional(readOnly = true)
    public TemplateResponseDTO getTemplate(Long templateId) {
        return templateRepository.findById(templateId)
                .map(templateMapper::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + templateId));
    }

    @Transactional(readOnly = true)
    public Page<TemplateResponseDTO> getAllTemplates(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return templateRepository.findAll(pageable)
                .map(templateMapper::toResponseDTO);
    }

    @Transactional
    public void deleteTemplate(Long templateId) {
        if (!templateRepository.existsById(templateId)) {
            throw new ResourceNotFoundException("Template not found: " + templateId);
        }
        templateRepository.deleteById(templateId);
    }

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
                .map(mapping -> {
                    TemplateField field = fieldMap.get(mapping.getFieldId());
                    if (field == null) {
                        throw new ResourceNotFoundException("Field not found: " + mapping.getFieldId());
                    }
                    if (!field.getDocumentTemplate().getId().equals(request.getTemplateId())) {
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

        DocumentTemplate template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + request.getTemplateId()));

        long mappedCount = template.getTemplateFields().stream()
                .filter(f -> f.getFieldLabel() != null && !f.getFieldLabel().isBlank())
                .count();

        template.setIsFullyMapped(!template.getTemplateFields().isEmpty() && mappedCount == template.getFieldCount());
        templateRepository.save(template);

        return savedFields.stream()
                .map(TemplateFieldResponseDTO::new)
                .toList();
    }

    public AvailableFieldResponseDTO getAvailableMappingFields() {
        return AvailableFieldResponseDTO.builder()
                .groups(List.of(
                        AvailableFieldResponseDTO.FieldGroup.builder()
                                .groupName("Informații Client (Automate)")
                                .options(List.of(
                                        new AvailableFieldResponseDTO.FieldOption("Nume / Denumire Client", "CLIENT_NAME", "Denumirea oficială a firmei sau persoanei."),
                                        new AvailableFieldResponseDTO.FieldOption("CUI Client", "CLIENT_CUI", "Codul Unic de Înregistrare."),
                                        new AvailableFieldResponseDTO.FieldOption("Adresa Sediu Social", "CLIENT_ADDRESS", "Adresa completă a sediului social."),
                                        new AvailableFieldResponseDTO.FieldOption("Tip Firma", "CLIENT_TYPE", "Forma juridică (SRL, PFA, etc.)."),
                                        new AvailableFieldResponseDTO.FieldOption("Administrație Fiscală", "CLIENT_ADMIN", "Administrația fiscală de care aparține.")
                                ))
                                .build(),
                        AvailableFieldResponseDTO.FieldGroup.builder()
                                .groupName("Informații Contract (Standard)")
                                .options(List.of(
                                        new AvailableFieldResponseDTO.FieldOption("Data Început", "CONTRACT_START_DATE", "Data de la care contractul devine activ."),
                                        new AvailableFieldResponseDTO.FieldOption("Data Sfârșit", "CONTRACT_END_DATE", "Data de expirare a contractului."),
                                        new AvailableFieldResponseDTO.FieldOption("Valoare Totală", "CONTRACT_VALUE", "Valoarea financiară a contractului."),
                                        new AvailableFieldResponseDTO.FieldOption("Notițe", "CONTRACT_NOTES", "Observații adiționale salvate la generare.")
                                ))
                                .build(),
                        AvailableFieldResponseDTO.FieldGroup.builder()
                                .groupName("Alte Opțiuni")
                                .options(List.of(
                                        new AvailableFieldResponseDTO.FieldOption("Manual (Input Utilizator)", "MANUAL", "Utilizatorul va introduce manual valoarea la fiecare generare.")
                                ))
                                .build()
                ))
                .build();
    }
}

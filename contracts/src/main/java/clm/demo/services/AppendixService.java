package clm.demo.services;

import clm.demo.dto.requests.GenAppendixRequest;
import clm.demo.dto.responses.AppendixResponseDTO;
import clm.demo.exceptions.*;
import clm.demo.mappers.AppendixMapper;
import clm.demo.models.Appendix;
import clm.demo.models.Contract;
import clm.demo.models.DocumentFieldValue;
import clm.demo.models.DocumentTemplate;
import clm.demo.models.TemplateField;
import clm.demo.models.enums.AppendixStatus;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.repositories.AppendixRepository;
import clm.demo.repositories.ContractRepository;
import clm.demo.repositories.DocumentFieldValueRepository;
import clm.demo.repositories.DocumentTemplateRepository;
import clm.demo.utils.Utils;
import clm.demo.utils.docx.DocxFiller;
import clm.demo.utils.file.FileUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class AppendixService {

    private final AppendixRepository appendixRepository;
    private final ContractRepository contractRepository;
    private final DocumentTemplateRepository templateRepository;
    private final DocumentFieldValueRepository fieldValueRepository;
    private final AppendixMapper appendixMapper;

    /**
     * Generates a fillable appendix from a template and attaches it to the given contract.
     */
    @Transactional
    public AppendixResponseDTO generateAppendix(@Valid GenAppendixRequest request) {
        Contract contract = contractRepository.findById(request.contractId())
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found: " + request.contractId()));

        DocumentTemplate template = templateRepository.findById(request.templateId())
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + request.templateId()));

        if (!template.getIsFullyMapped()) {
            throw new TemplateIncompleteException("Template " + template.getId() + " is not fully mapped.");
        }

        validateMandatoryFields(template, request.mappings());

        Appendix appendix = Appendix.builder()
                .contract(contract)
                .documentTemplate(template)
                .title(request.title())
                .generatedBy(request.userId() != null ? request.userId().intValue() : null)
                .generatedByMail(request.userMail())
                .notes(request.notes())
                .appendixStatus(AppendixStatus.DRAFT)
                .build();

        appendix = appendixRepository.save(appendix);

        List<DocumentFieldValue> fieldValues = buildFieldValues(appendix, template, request.mappings());
        if (!fieldValues.isEmpty()) {
            fieldValueRepository.saveAll(fieldValues);
            appendix.setFieldValues(fieldValues);
        }

        try {
            List<TemplateField> ordered = template.getTemplateFields().stream()
                    .filter(f -> f.getFieldPosition() != null && f.getFieldLabel() != null)
                    .sorted(java.util.Comparator.comparingInt(TemplateField::getFieldPosition))
                    .toList();
            Map<String, String> labelToValue = buildLabelValueMap(fieldValues);
            byte[] templateBytes = FileUtils.decompress(template.getDocumentContent());
            byte[] filled = DocxFiller.fillDocx(templateBytes, ordered, labelToValue);
            byte[] pdf = FileUtils.convert(filled, DocumentFormat.DOCX, DocumentFormat.PDF);
            appendix.setDocumentContent(FileUtils.compress(pdf));
            appendix.setDocumentFormat(DocumentFormat.PDF);
            appendix = appendixRepository.save(appendix);
        } catch (IOException e) {
            throw new ContractGenerationFailException("Failed to generate appendix document: " + e.getMessage());
        }

        return appendixMapper.toResponseDTO(appendix);
    }

    /**
     * Uploads a non-fillable appendix directly (no template, no field values).
     * Format is auto-detected from file magic bytes.
     */
    @Transactional
    public AppendixResponseDTO uploadDirectAppendix(Long contractId, String title, byte[] fileBytes,
                                                     Integer userId, String userMail, String notes) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found: " + contractId));

        DocumentFormat format = Utils.detectDocumentFormat(fileBytes);

        try {
            Appendix appendix = Appendix.builder()
                    .contract(contract)
                    .title(title)
                    .generatedBy(userId)
                    .generatedByMail(userMail)
                    .notes(notes)
                    .documentContent(FileUtils.compress(fileBytes))
                    .documentFormat(format)
                    .appendixStatus(AppendixStatus.DRAFT)
                    .build();

            return appendixMapper.toResponseDTO(appendixRepository.save(appendix));
        } catch (IOException e) {
            throw new FileConversionException("Failed to store appendix document: " + e.getMessage(), e);
        }
    }

    /**
     * Uploads a signed appendix, auto-converts to PDF, and transitions status to SIGNED.
     */
    @Transactional
    public AppendixResponseDTO uploadSignedAppendix(Long appendixId, byte[] fileBytes) {
        Appendix appendix = appendixRepository.findById(appendixId)
                .orElseThrow(() -> new ResourceNotFoundException("Appendix not found: " + appendixId));

        try {
            DocumentFormat sourceFormat = Utils.detectDocumentFormat(fileBytes);
            byte[] pdfBytes = sourceFormat != DocumentFormat.PDF
                    ? FileUtils.convert(fileBytes, sourceFormat, DocumentFormat.PDF)
                    : fileBytes;

            appendix.setSignedDocumentContent(FileUtils.compress(pdfBytes));
            appendix.setAppendixStatus(AppendixStatus.SIGNED);
            appendix = appendixRepository.save(appendix);
        } catch (IOException e) {
            throw new FileConversionException("Failed to process signed appendix: " + e.getMessage(), e);
        }

        return appendixMapper.toResponseDTO(appendix);
    }

    @Transactional(readOnly = true)
    public List<AppendixResponseDTO> getAppendicesForContract(Long contractId) {
        if (!contractRepository.existsById(contractId)) {
            throw new ResourceNotFoundException("Contract not found: " + contractId);
        }
        return appendixRepository.findByContractId(contractId)
                .stream()
                .map(appendixMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public void deleteAppendix(Long appendixId) {
        if (!appendixRepository.existsById(appendixId)) {
            throw new ResourceNotFoundException("Appendix not found: " + appendixId);
        }
        appendixRepository.deleteById(appendixId);
    }

    private void validateMandatoryFields(DocumentTemplate template, Map<String, String> mappings) {
        List<String> missing = template.getTemplateFields().stream()
                .filter(TemplateField::getIsRequired)
                .filter(f -> f.getFieldLabel() != null)
                .filter(f -> !mappings.containsKey(f.getFieldLabel()) || mappings.get(f.getFieldLabel()).isBlank())
                .map(TemplateField::getFieldLabel)
                .toList();

        if (!missing.isEmpty()) {
            throw new MissingMandatoryFieldException(
                    "Missing mandatory field mappings: " + String.join(", ", missing), missing);
        }
    }

    private List<DocumentFieldValue> buildFieldValues(Appendix appendix, DocumentTemplate template,
                                                       Map<String, String> mappings) {
        List<DocumentFieldValue> fieldValues = new ArrayList<>();
        for (TemplateField field : template.getTemplateFields()) {
            if (field.getFieldLabel() == null) continue;
            String value = mappings.get(field.getFieldLabel());
            if (value == null || value.isBlank()) continue;
            fieldValues.add(DocumentFieldValue.builder()
                    .document(appendix)
                    .templateField(field)
                    .fieldValue(value)
                    .build());
        }
        return fieldValues;
    }

    private static Map<String, String> buildLabelValueMap(List<DocumentFieldValue> fieldValues) {
        Map<String, String> map = new java.util.HashMap<>(fieldValues.size() * 2);
        for (DocumentFieldValue dfv : fieldValues) {
            TemplateField field = dfv.getTemplateField();
            if (field != null && field.getFieldLabel() != null && dfv.getFieldValue() != null) {
                map.put(field.getFieldLabel(), dfv.getFieldValue());
            }
        }
        return map;
    }
}

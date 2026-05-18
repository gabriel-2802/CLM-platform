package clm.demo.services;

import clm.demo.dto.requests.GenAppendixRequest;
import clm.demo.dto.requests.UploadDirectAppendixRequest;
import clm.demo.dto.responses.AppendixResponseDTO;
import clm.demo.exceptions.exceptions.*;
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
import clm.demo.services.utility.DocumentGenerationUtil;
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
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class AppendixService {

    private final AppendixRepository           appendixRepository;
    private final ContractRepository           contractRepository;
    private final DocumentTemplateRepository   templateRepository;
    private final DocumentFieldValueRepository fieldValueRepository;
    private final AppendixMapper               appendixMapper;
    private final FileUtils                    fileUtils;
    private final DocumentGenerationUtil       documentGenerationUtil;

    /**
     * Generates a fillable appendix from a template and attaches it to the given contract.
     * The appendix is created in {@link AppendixStatus#DRAFT} status; it transitions to
     * {@link AppendixStatus#SIGNED} only when a signed document is uploaded via
     * {@link #uploadSignedAppendix}.
     */
    @Transactional
    public AppendixResponseDTO generateAppendix(@Valid GenAppendixRequest request) {
        log.info("Generating appendix for contract {} from template {}",
                request.contractId(), request.templateId());

        Contract contract = contractRepository.findById(request.contractId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Contract not found: " + request.contractId()));

        DocumentTemplate template = templateRepository.findById(request.templateId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Template not found: " + request.templateId()));

        if (!template.getIsFullyMapped()) {
            throw new TemplateIncompleteException(
                    "Template " + template.getId() + " is not fully mapped.");
        }

        documentGenerationUtil.validateMandatoryFields(template, request.mappings());

        Appendix appendix = Appendix.builder()
                .contract(contract)
                .documentTemplate(template)
                .title(request.title())
                .generatedByUser(Objects.nonNull(request.userId()) ? request.userId().intValue() : null)
                .notes(request.notes())
                .appendixStatus(AppendixStatus.DRAFT)
                .build();

        appendix = appendixRepository.save(appendix);
        log.debug("Appendix {} created in DRAFT status for contract {}",
                appendix.getId(), contract.getId());

        List<DocumentFieldValue> fieldValues =
                documentGenerationUtil.buildFieldValues(appendix, template, request.mappings());

        if (!fieldValues.isEmpty()) {
            fieldValueRepository.saveAll(fieldValues);
            appendix.setFieldValues(fieldValues);
        }

        try {
            appendix = fillAndPersistDocument(appendix, template, fieldValues);
            log.info("Appendix {} generated successfully ({} field(s) filled)",
                    appendix.getId(), fieldValues.size());
        } catch (IOException e) {
            throw new ContractGenerationFailException(
                    "Failed to generate appendix document: " + e.getMessage());
        }

        return appendixMapper.toResponseDTO(appendix);
    }

    /**
     * Uploads a non-fillable appendix directly (no template, no field values).
     * The appendix is immediately set to {@link AppendixStatus#SIGNED} since the
     * uploaded file is treated as the final, authoritative document.
     * Format is auto-detected from file magic bytes.
     */
    @Transactional
    public AppendixResponseDTO uploadDirectAppendix(@Valid UploadDirectAppendixRequest request) {
        log.info("Uploading direct appendix for contract {}", request.getContractId());

        Contract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Contract not found: " + request.getContractId()));

        try {
            byte[] fileBytes      = request.getFile().getBytes();
            DocumentFormat format = Utils.detectDocumentFormat(fileBytes);

            Appendix appendix = Appendix.builder()
                    .contract(contract)
                    .title(request.getTitle())
                    .generatedByUser(request.getUserId())
                    .notes(request.getNotes())
                    .signedDocumentContent(fileUtils.compress(fileBytes))
                    .documentFormat(format)
                    .uploadedSignedAt(LocalDateTime.now())
                    .uploadedSignedByUser(request.getUserId())
                    .appendixStatus(AppendixStatus.SIGNED)
                    .build();

            AppendixResponseDTO response = appendixMapper.toResponseDTO(appendixRepository.save(appendix));
            log.info("Direct appendix {} uploaded as SIGNED for contract {}",
                    response.getId(), contract.getId());
            return response;
        } catch (IOException e) {
            throw new FileConversionException(
                    "Failed to store appendix document: " + e.getMessage(), e);
        }
    }

    /**
     * Uploads a signed document for an existing {@link AppendixStatus#DRAFT} appendix,
     * auto-converts it to PDF, and transitions the status to {@link AppendixStatus#SIGNED}.
     *
     * @throws InvalidAppendixStateException if the appendix is already {@link AppendixStatus#SIGNED}
     */
    @Transactional
    public AppendixResponseDTO uploadSignedAppendix(Long appendixId, byte[] fileBytes, Integer userId) {
        log.info("Uploading signed document for appendix {}", appendixId);

        Appendix appendix = appendixRepository.findById(appendixId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appendix not found: " + appendixId));

        if (appendix.getAppendixStatus() == AppendixStatus.SIGNED) {
            throw new InvalidAppendixStateException(
                    "Appendix " + appendixId + " is already SIGNED and cannot accept another signed document.");
        }

        try {
            DocumentFormat sourceFormat = Utils.detectDocumentFormat(fileBytes);
            byte[] pdfBytes = sourceFormat != DocumentFormat.PDF
                    ? fileUtils.convert(fileBytes, sourceFormat, DocumentFormat.PDF)
                    : fileBytes;

            appendix.setSignedDocumentContent(fileUtils.compress(pdfBytes));
            appendix.setUploadedSignedAt(LocalDateTime.now());
            appendix.setUploadedSignedByUser(userId);
            appendix.setAppendixStatus(AppendixStatus.SIGNED);
            appendix = appendixRepository.save(appendix);
            log.info("Appendix {} transitioned DRAFT → SIGNED", appendixId);
        } catch (IOException e) {
            throw new FileConversionException(
                    "Failed to process signed appendix: " + e.getMessage(), e);
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
    public AppendixResponseDTO terminateAppendix(Long appendixId) {
        Appendix appendix = appendixRepository.findById(appendixId)
                .orElseThrow(() -> new ResourceNotFoundException("Appendix not found: " + appendixId));

        if (appendix.getAppendixStatus() != AppendixStatus.SIGNED) {
            throw new InvalidAppendixStateException(
                    "Appendix " + appendixId + " cannot be terminated: only SIGNED appendices can be terminated.");
        }

        appendix.setAppendixStatus(AppendixStatus.TERMINATED);
        appendix = appendixRepository.save(appendix);
        log.info("Appendix {} terminated", appendixId);
        return appendixMapper.toResponseDTO(appendix);
    }

    @Transactional
    public void deleteAppendix(Long appendixId) {
        if (!appendixRepository.existsById(appendixId)) {
            throw new ResourceNotFoundException("Appendix not found: " + appendixId);
        }
        appendixRepository.deleteById(appendixId);
        log.info("Appendix {} deleted", appendixId);
    }

    private Appendix fillAndPersistDocument(
            Appendix appendix,
            DocumentTemplate template,
            List<DocumentFieldValue> fieldValues) throws IOException {

        List<TemplateField> ordered = template.getTemplateFields().stream()
                .filter(f -> Objects.nonNull(f.getFieldPosition()) && Objects.nonNull(f.getFieldLabel()))
                .sorted(Comparator.comparingInt(TemplateField::getFieldPosition))
                .toList();

        Map<String, String> labelToValue = documentGenerationUtil.buildLabelValueMap(fieldValues);
        byte[] templateBytes = fileUtils.decompress(template.getDocumentContent());
        byte[] filled        = DocxFiller.fillDocx(templateBytes, ordered, labelToValue);
        byte[] pdf           = fileUtils.convert(filled, DocumentFormat.DOCX, DocumentFormat.PDF);

        appendix.setDocumentContent(fileUtils.compress(pdf));
        appendix.setDocumentFormat(DocumentFormat.PDF);
        return appendixRepository.save(appendix);
    }
}
package clm.demo.services;

import clm.demo.dto.requests.GenContractRequest;
import clm.demo.dto.responses.ContractResponseDTO;
import clm.demo.exceptions.FileConversionException;
import clm.demo.exceptions.MissingMandatoryFieldException;
import clm.demo.exceptions.ResourceNotFoundException;
import clm.demo.exceptions.TemplateIncompleteException;
import clm.demo.exceptions.UnsupportedFileException;
import clm.demo.mappers.ContractGenerationMapper;
import clm.demo.mappers.GeneratedContractMapper;
import clm.demo.models.Contract;
import clm.demo.models.ContractFieldValue;
import clm.demo.models.Template;
import clm.demo.models.TemplateField;
import clm.demo.models.enums.ContractStatus;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.repositories.ContractFieldValueRepository;
import clm.demo.repositories.ContractRepository;
import clm.demo.repositories.TemplateRepository;
import clm.demo.services.file.actions.FileContentReplacementService;
import clm.demo.services.file.actions.FileConverterService;
import clm.demo.services.file.actions.FileZipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
@Transactional
public class ContractService {

    private final TemplateRepository contractTemplateRepository;
    private final ContractRepository generatedContractRepository;
    private final ContractFieldValueRepository contractFieldValueRepository;
    private final ContractGenerationMapper contractGenerationMapper;
    private final GeneratedContractMapper generatedContractMapper;
    private final FileContentReplacementService fileContentReplacementService;
    private final FileZipService zipService;
    private final FileConverterService fileConverterService;

    /**
     * Generates a new contract from a template with provided field mappings.
     *
     * @param request the contract generation request
     * @return a ContractResponseDTO with the newly generated contract details
     * @throws ResourceNotFoundException      if template is not found
     * @throws TemplateIncompleteException    if template is not fully mapped
     * @throws MissingMandatoryFieldException if required fields are missing values
     */
    public ContractResponseDTO generateContract(@Valid GenContractRequest request) {
        log.info("Starting contract generation for template ID: {}, client ID: {}",
                request.getTemplateId(), request.getClientId());

        Template template = contractTemplateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with ID: " + request.getTemplateId()));

        if (!template.getIsFullyMapped()) {
            throw new TemplateIncompleteException("Template " + template.getId() + " is not fully mapped.");
        }

        validateMandatoryFields(template, request.getMappings());

        // 1. Save contract shell first to get an ID
        Contract contract = contractGenerationMapper.toContractEntity(request, template);
        contract = generatedContractRepository.save(contract);
        log.info("Contract shell saved with ID: {}", contract.getId());

        // 2. Build field values against the persisted contract
        List<ContractFieldValue> fieldValues = buildFieldValues(contract, template, request.getMappings());
        if (!fieldValues.isEmpty()) {
            contractFieldValueRepository.saveAll(fieldValues);
            contract.setFieldValues(fieldValues);
            log.info("Persisted {} field values for contract ID: {}", fieldValues.size(), contract.getId());
        }

        // 3. Generate document content and update contract
        try {
            byte[] documentContent = fileContentReplacementService.generateDocumentContent(contract, template, fieldValues);
            contract.setDocumentContent(zipService.compress(documentContent));
            contract = generatedContractRepository.save(contract);
            log.info("Contract document saved for ID: {}", contract.getId());
        } catch (IOException e) {
            throw new RuntimeException("Document generation failed: " + e.getMessage(), e);
        }

        return generatedContractMapper.toResponseDTO(contract);
    }

    /**
     * Uploads a signed contract document, converts it to PDF if necessary,
     * compresses it, and updates the contract status to ACTIVE.
     *
     * @param contractId the ID of the contract to update
     * @param fileBytes  the signed document file bytes (DOCX or PDF)
     * @return ContractResponseDTO with updated contract details
     * @throws ResourceNotFoundException if contract is not found
     * @throws FileConversionException   if document processing fails
     */
    public ContractResponseDTO uploadSignedContract(Long contractId, byte[] fileBytes) {
        log.info("Processing signed document upload for contract ID: {}", contractId);

        // Fetch the contract
        Contract contract = generatedContractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with ID: " + contractId));

        try {
            // Detect the file format based on file content (PDF or DOCX)
            DocumentFormat sourceFormat = detectDocumentFormat(fileBytes);
            log.debug("Detected file format: {}", sourceFormat);

            // Convert to PDF if not already in PDF format
            byte[] pdfBytes = fileBytes;
            if (sourceFormat != DocumentFormat.PDF) {
                log.info("Converting document from {} to PDF", sourceFormat);
                pdfBytes = fileConverterService.convert(fileBytes, sourceFormat, DocumentFormat.PDF);
            }

            // Compress the PDF document
            byte[] compressedPdf = zipService.compress(pdfBytes);

            // Update contract with signed document and set status to ACTIVE
            contract.setSignedDocument(compressedPdf);
            contract.setContractStatus(ContractStatus.ACTIVE);

            log.info("Signed document uploaded and contract status updated to ACTIVE for contract ID: {}", contractId);

        } catch (IOException e) {
            log.error("Document processing failed for contract ID {}: {}", contractId, e.getMessage(), e);
            throw new FileConversionException("Failed to process signed document: " + e.getMessage(), e);
        }

        return generatedContractMapper.toResponseDTO(contract);
    }

    /**
     * Detects the document format (PDF or DOCX) from the file bytes.
     *
     * @param fileBytes the raw file bytes
     * @return DocumentFormat.PDF or DocumentFormat.DOCX
     * @throws UnsupportedFileException if the format cannot be detected
     */
    private DocumentFormat detectDocumentFormat(byte[] fileBytes) {
        if (fileBytes == null || fileBytes.length < 4) {
            throw new UnsupportedFileException("Invalid file: insufficient data to determine format");
        }

        // Check for PDF signature (PDF files start with "%PDF")
        if (fileBytes[0] == 0x25 && fileBytes[1] == 0x50 && 
            fileBytes[2] == 0x44 && fileBytes[3] == 0x46) {
            return DocumentFormat.PDF;
        }

        // Check for DOCX signature (ZIP format starting with PK)
        // DOCX files are ZIP archives that start with "PK"
        if (fileBytes[0] == 0x50 && fileBytes[1] == 0x4B) {
            return DocumentFormat.DOCX;
        }

        throw new UnsupportedFileException("Unable to detect document format. Supported formats: PDF, DOCX");
    }


    private void validateMandatoryFields(Template template, Map<String, String> mappings) {
        List<String> missingFields = template.getTemplateFields().stream()
                .filter(TemplateField::getIsRequired)
                .filter(field -> field.getFieldLabel() != null)
                .filter(field -> !mappings.containsKey(field.getFieldLabel())
                        || mappings.get(field.getFieldLabel()).isBlank())
                .map(TemplateField::getFieldLabel)
                .toList();

        if (!missingFields.isEmpty()) {
            String message = "Missing mandatory field mappings: " + String.join(", ", missingFields);
            log.warn(message);
            throw new MissingMandatoryFieldException(message, missingFields);
        }
    }

    private List<ContractFieldValue> buildFieldValues(Contract contract, Template template, Map<String, String> mappings) {

        List<ContractFieldValue> fieldValues = new ArrayList<>();

        for (TemplateField field : template.getTemplateFields()) {
            if (field.getFieldLabel() == null) continue;

            String value = mappings.get(field.getFieldLabel());

            if (value == null || value.isBlank()) continue;

            fieldValues.add(ContractFieldValue.builder()
                    .contract(contract)
                    .templateField(field)
                    .fieldValue(value)
                    .build());
        }
        return fieldValues;
    }

    public ResponseEntity<List<ContractResponseDTO>> getAll() {
        return generatedContractRepository.findAll().stream()
                .map(generatedContractMapper::toResponseDTO)
                .toList()
                .isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(generatedContractRepository.findAll().stream()
                        .map(generatedContractMapper::toResponseDTO)
                        .toList());
    }
}
package clm.demo.services;

import clm.demo.dto.requests.ContractTerminationRequest;
import clm.demo.dto.requests.GenContractRequest;
import clm.demo.dto.requests.SearchRequest;
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
import clm.demo.specifications.ContractSpecification;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    private static final int DEFAULT_PAGE      = 0;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final TemplateRepository contractTemplateRepository;
    private final ContractRepository generatedContractRepository;
    private final ContractFieldValueRepository contractFieldValueRepository;
    private final ContractGenerationMapper contractGenerationMapper;
    private final GeneratedContractMapper generatedContractMapper;
    private final FileContentReplacementService fileContentReplacementService;
    private final FileZipService zipService;
    private final FileConverterService fileConverterService;
    private final ContractSpecification contractSpecification;

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
                request.templateId(), request.clientId());

        Template template = contractTemplateRepository.findById(request.templateId())
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with ID: " + request.templateId()));

        if (!template.getIsFullyMapped()) {
            throw new TemplateIncompleteException("Template " + template.getId() + " is not fully mapped.");
        }

        validateMandatoryFields(template, request.mappings());

        // 1. Save contract shell first to get an ID
        Contract contract = contractGenerationMapper.toContractEntity(request, template);
        contract = generatedContractRepository.save(contract);
        log.info("Contract shell saved with ID: {}", contract.getId());

        // 2. Build field values against the persisted contract
        List<ContractFieldValue> fieldValues = buildFieldValues(contract, template, request.mappings());
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

        Contract contract = generatedContractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with ID: " + contractId));

        try {
            DocumentFormat sourceFormat = detectDocumentFormat(fileBytes);
            log.debug("Detected file format: {}", sourceFormat);

            byte[] pdfBytes = fileBytes;
            if (sourceFormat != DocumentFormat.PDF) {
                log.info("Converting document from {} to PDF", sourceFormat);
                pdfBytes = fileConverterService.convert(fileBytes, sourceFormat, DocumentFormat.PDF);
            }

            contract.setSignedDocument(zipService.compress(pdfBytes));
            contract.setContractStatus(ContractStatus.ACTIVE);
            contract = generatedContractRepository.save(contract);

            log.info("Signed document uploaded and contract status updated to ACTIVE for contract ID: {}", contractId);

        } catch (IOException e) {
            log.error("Document processing failed for contract ID {}: {}", contractId, e.getMessage(), e);
            throw new FileConversionException("Failed to process signed document: " + e.getMessage(), e);
        }

        return generatedContractMapper.toResponseDTO(contract);
    }

    /**
     * Terminates a contract by updating its status to TERMINATED,
     * setting the termination date and reasons for termination.
     *
     * @param contractId the ID of the contract to terminate
     * @param request    the termination request containing termination date and reasons
     * @throws ResourceNotFoundException if contract is not found
     */
    public void terminateContract(Long contractId, @Valid ContractTerminationRequest request) {
        log.info("Terminating contract ID: {} with termination date: {}", contractId, request.getTerminationDate());

        Contract contract = generatedContractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with ID: " + contractId));

        contract.setContractStatus(ContractStatus.TERMINATED);
        contract.setTerminationDate(request.getTerminationDate().toLocalDate());
        contract.setReasonsForTermination(request.getReasons() != null ? request.getReasons() : "");

        generatedContractMapper.toResponseDTO(contract);
    }

    public ResponseEntity<List<ContractResponseDTO>> getAll() {
        List<ContractResponseDTO> list = generatedContractRepository.findAll().stream()
                .map(generatedContractMapper::toResponseDTO)
                .toList();
        return list.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(list);
    }

    /**
     * Searches contracts using server-side filtering, sorting, and pagination.
     *
     * <p>All predicates (notes, status, clientId, generatedBy, templateName,
     * templateDescription, date range, labelValues) are translated to SQL by
     * {@link ContractSpecification} and executed entirely on PostgreSQL.
     * No rows are loaded into the JVM before the final page is assembled.</p>
     *
     * <p>Pagination is applied via {@code LIMIT} / {@code OFFSET} in the
     * generated SQL.  Results are ordered by {@code created_at DESC} so the
     * most recent contracts appear first.</p>
     *
     * @param request filter and pagination parameters
     * @return 200 OK with the result page, or 204 No Content when empty
     */
    @Transactional(readOnly = true)
    public ResponseEntity<List<ContractResponseDTO>> search(SearchRequest request) {
        log.info("Searching contracts with criteria: {}", request);

        int pageIndex = request.page() != null ? request.page() : DEFAULT_PAGE;
        int pageSize  = request.size() != null ? request.size() : DEFAULT_PAGE_SIZE;

        PageRequest pageable = PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Contract> page = generatedContractRepository.findAll(
                contractSpecification.buildSearchSpecification(request),
                pageable
        );

        log.debug("Search returned {}/{} contracts (page {}/{})",
                page.getNumberOfElements(), page.getTotalElements(),
                page.getNumber(), page.getTotalPages());

        List<ContractResponseDTO> responseDTOs = page.getContent().stream()
                .map(generatedContractMapper::toResponseDTO)
                .toList();

        return responseDTOs.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(responseDTOs);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private DocumentFormat detectDocumentFormat(byte[] fileBytes) {
        if (fileBytes == null || fileBytes.length < 4) {
            throw new UnsupportedFileException("Invalid file: insufficient data to determine format");
        }
        if (fileBytes[0] == 0x25 && fileBytes[1] == 0x50 &&
            fileBytes[2] == 0x44 && fileBytes[3] == 0x46) {
            return DocumentFormat.PDF;
        }
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
}

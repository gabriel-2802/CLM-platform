package clm.demo.services;

import clm.demo.dto.requests.ContractTerminationRequest;
import clm.demo.dto.requests.GenContractRequest;
import clm.demo.dto.requests.SearchRequest;
import clm.demo.dto.responses.ContractResponseDTO;
import clm.demo.exceptions.*;
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
import clm.demo.utils.docx.DocxFiller;
import clm.demo.utils.file.FileUtils;
import clm.demo.specifications.ContractSpecification;
import clm.demo.utils.Utils;
import jakarta.validation.Valid;
import org.springframework.jdbc.core.JdbcTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import static clm.demo.utils.Constants.DEFAULT_PAGE;
import static clm.demo.utils.Constants.DEFAULT_PAGE_SIZE;


/**
 * Service class for Generated Contracts
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class ContractService {

    private final TemplateRepository contractTemplateRepository;
    private final ContractRepository generatedContractRepository;
    private final ContractFieldValueRepository contractFieldValueRepository;

    private final ContractGenerationMapper contractGenerationMapper;
    private final GeneratedContractMapper generatedContractMapper;

    private final ContractSpecification contractSpecification;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Generates a new contract from a template with provided field mappings.
     *
     * @param request the contract generation request
     * @return a ContractResponseDTO with the newly generated contract details
     * @throws ResourceNotFoundException      if template is not found
     * @throws MissingMandatoryFieldException if required fields are missing values
     */
    @Transactional
    public ContractResponseDTO generateContract(@Valid GenContractRequest request) {
        Template template = contractTemplateRepository.findById(request.templateId())
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with ID: " + request.templateId()));

        // fetch client data from the public schema and merge into mappings
        Map<String, String> autoData = fetchClientData(request.clientId());
        
        // Add contract metadata to autoData
        autoData.put("CONTRACT_START_DATE", request.startDate() != null ? request.startDate().toString() : "");
        autoData.put("CONTRACT_END_DATE", request.endDate() != null ? request.endDate().toString() : "");
        autoData.put("CONTRACT_VALUE", request.value() != null ? String.format("%.2f", request.value()) : "");
        autoData.put("CONTRACT_NOTES", request.notes() != null ? request.notes() : "");

        Map<String, String> mergedMappings = new HashMap<>(request.mappings());
        autoData.forEach((key, value) -> mergedMappings.putIfAbsent(key, value));

        validateMandatoryFields(template, mergedMappings);

        // save early to obtain a DB-assigned ID required by ContractFieldValue FK.
        Contract contract = contractGenerationMapper.toContractEntity(request, template);
        contract = generatedContractRepository.save(contract);

        // build field values against the persisted contract
        List<ContractFieldValue> fieldValues = buildFieldValues(contract, template, mergedMappings);
        if (!fieldValues.isEmpty()) {
            contractFieldValueRepository.saveAll(fieldValues);
            contract.setFieldValues(fieldValues);
        }

        // generate document content and update contract
        try {
            List<TemplateField> ordered = template.getTemplateFields().stream()
                    .filter(f -> f.getFieldPosition() != null && f.getFieldLabel() != null)
                    .sorted(java.util.Comparator.comparingInt(TemplateField::getFieldPosition))
                    .toList();
            Map<String, String> labelToValue = buildLabelValueMap(fieldValues);
            byte[] templateBytes = FileUtils.decompress(template.getDocumentContent());
            byte[] filled = DocxFiller.fillDocx(templateBytes, ordered, labelToValue);
            byte[] documentContent = FileUtils.convert(filled, DocumentFormat.DOCX, DocumentFormat.PDF);
            contract.setDocumentContent(FileUtils.compress(documentContent));
            contract = generatedContractRepository.save(contract);
        } catch (IOException e) {
            throw new ContractGenerationFailException("Failed to generate contract document: " + e.getMessage());
        }

        return generatedContractMapper.toResponseDTO(contract);
    }

    /**
     * Fetches client details from the public schema using JdbcTemplate.
     * This bridges the two isolated schemas for data injection.
     *
     * @param clientId the ID of the client in the public schema
     * @return a map of property labels to their database values
     */
    private Map<String, String> fetchClientData(Long clientId) {
        String sql = "SELECT denumire as name, cui, adresa as address, tip as type, administratie as admin FROM public.\"Client\" WHERE id = ?";
        try {
            Map<String, Object> data = jdbcTemplate.queryForMap(sql, clientId);
            Map<String, String> result = new HashMap<>();
            result.put("CLIENT_NAME", String.valueOf(data.getOrDefault("name", "")));
            result.put("CLIENT_CUI", String.valueOf(data.getOrDefault("cui", "")));
            result.put("CLIENT_ADDRESS", String.valueOf(data.getOrDefault("address", "")));
            result.put("CLIENT_TYPE", String.valueOf(data.getOrDefault("type", "")));
            result.put("CLIENT_ADMIN", String.valueOf(data.getOrDefault("admin", "")));
            return result;
        } catch (Exception e) {
            log.warn("Failed to fetch client data for ID {}: {}", clientId, e.getMessage());
            return new HashMap<>();
        }
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
    @Transactional
    public ContractResponseDTO uploadSignedContract(Long contractId, byte[] fileBytes) {
        Contract contract = generatedContractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with ID: " + contractId));

        try {
            DocumentFormat sourceFormat = Utils.detectDocumentFormat(fileBytes);

            byte[] pdfBytes = fileBytes;
            if (sourceFormat != DocumentFormat.PDF) {
                pdfBytes = FileUtils.convert(fileBytes, sourceFormat, DocumentFormat.PDF);
            }

            contract.setSignedDocument(FileUtils.compress(pdfBytes));
            contract.setContractStatus(ContractStatus.ACTIVE);
            contract = generatedContractRepository.save(contract);

        } catch (IOException e) {
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
     * @throws ResourceNotFoundException    if contract is not found
     * @throws InvalidContractStateException if contract is not in a terminable state
     */
    @Transactional
    public void terminateContract(Long contractId, @Valid ContractTerminationRequest request) {
        Contract contract = generatedContractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with ID: " + contractId));

        if (contract.getContractStatus() != ContractStatus.ACTIVE) {
            throw new InvalidContractStateException(
                    "Cannot terminate contract in status: " + contract.getContractStatus() +
                            ". Only ACTIVE contracts can be terminated."
            );
        }

        contract.setContractStatus(ContractStatus.TERMINATED);
        contract.setTerminationDate(request.getTerminationDate().toLocalDate());
        contract.setReasonsForTermination(request.getReasons());

        generatedContractRepository.save(contract);
    }

    /**
     * Returns all contracts with pagination.
     *
     * @param page zero-based page index (default 0)
     * @param size number of records per page (default 20)
     * @return a page of ContractResponseDTOs
     */
    @Transactional(readOnly = true)
    public Page<ContractResponseDTO> getAll(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return generatedContractRepository.findAll(pageable)
                .map(generatedContractMapper::toResponseDTO);
    }

    /**
     * Returns a paginated, filtered list of contracts.
     *
     * <p>All predicates (notes, status, clientId, generatedBy, templateName,
     * templateDescription, date range, labelValues) are translated to SQL by
     * {@link ContractSpecification} and executed entirely on PostgreSQL.
     * No rows are loaded into the JVM before the final page is assembled.</p>
     *
     * <p>Pagination is applied via {@code LIMIT} / {@code OFFSET} in the
     * generated SQL. Results are ordered by {@code created_at DESC} so the
     * most recent contracts appear first.</p>
     *
     * @param request filter and pagination parameters
     * @return a page of matching ContractResponseDTOs; empty page if none found
     */
    @Transactional(readOnly = true)
    public Page<ContractResponseDTO> search(SearchRequest request) {
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

        return page.map(generatedContractMapper::toResponseDTO);
    }

    /**
     * Validates that all required template fields have non-empty values in the provided mappings.
     *
     * @param template the template containing field definitions with required flags
     * @param mappings map of field labels to field values
     * @throws MissingMandatoryFieldException if any required field is missing or has a blank value
     */
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

    /**
     * Builds a list of ContractFieldValue entities from template fields and provided mappings.
     *
     *
     * @param contract the contract entity to associate field values with
     * @param template the template containing field definitions
     * @param mappings map of field labels to field values
     * @return a list of ContractFieldValue entities; empty list if no fields have values
     */
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

    /**
     * Builds a map from field labels to their string values.
     *
     * <p>entries where the field, field label, or field value is null are excluded.
     * a missing value is intentional: the corresponding placeholder is left intact in
     * the output document rather than being replaced with an empty string.</p>
     *
     * @param fieldValues list of contract field values
     * @return map from field label to field value, never null
     */
    private static Map<String, String> buildLabelValueMap(List<ContractFieldValue> fieldValues) {
        Map<String, String> map = new java.util.HashMap<>(fieldValues.size() * 2);
        for (ContractFieldValue cfv : fieldValues) {
            TemplateField field = cfv.getTemplateField();
            // null values excluded intentionally: a missing value leaves the placeholder intact
            if (field != null && field.getFieldLabel() != null && cfv.getFieldValue() != null) {
                map.put(field.getFieldLabel(), cfv.getFieldValue());
            }
        }
        return map;
    }
}
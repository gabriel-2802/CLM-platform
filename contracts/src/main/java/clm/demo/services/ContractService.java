package clm.demo.services;

import clm.demo.dto.requests.ContractTerminationRequest;
import clm.demo.dto.requests.GenContractRequest;
import clm.demo.dto.requests.SearchRequest;
import clm.demo.dto.responses.ContractResponseDTO;
import clm.demo.exceptions.*;
import clm.demo.mappers.ContractGenerationMapper;
import clm.demo.mappers.GeneratedContractMapper;
import clm.demo.models.Contract;
import clm.demo.models.DocumentFieldValue;
import clm.demo.models.DocumentTemplate;
import clm.demo.models.TemplateField;
import clm.demo.models.enums.ContractStatus;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.repositories.ContractRepository;
import clm.demo.repositories.DocumentFieldValueRepository;
import clm.demo.repositories.DocumentTemplateRepository;
import clm.demo.specifications.ContractSpecification;
import clm.demo.utils.Utils;
import clm.demo.utils.docx.DocxFiller;
import clm.demo.utils.file.FileUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static clm.demo.utils.Constants.DEFAULT_PAGE;
import static clm.demo.utils.Constants.DEFAULT_PAGE_SIZE;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class ContractService {

    private final DocumentTemplateRepository templateRepository;
    private final ContractRepository contractRepository;
    private final DocumentFieldValueRepository fieldValueRepository;

    private final ContractGenerationMapper generationMapper;
    private final GeneratedContractMapper contractMapper;

    private final ContractSpecification contractSpecification;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Generates a new contract from a template with provided field mappings.
     * Client data (name, CUI, address, etc.) is automatically fetched from the
     * public schema and merged with user-provided mappings before field injection.
     *
     * @param request the contract generation request
     * @return a ContractResponseDTO with the newly generated contract details
     * @throws ResourceNotFoundException      if template is not found
     * @throws MissingMandatoryFieldException if required fields are missing values
     */
    @Transactional
    public ContractResponseDTO generateContract(@Valid GenContractRequest request) {
        DocumentTemplate template = templateRepository.findById(request.templateId())
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + request.templateId()));

        if (!template.getIsFullyMapped()) {
            throw new TemplateIncompleteException("Template " + template.getId() + " is not fully mapped.");
        }

        Map<String, String> autoData = fetchClientData(request.clientId());
        autoData.put("CONTRACT_START_DATE", request.startDate() != null ? request.startDate().toString() : "");
        autoData.put("CONTRACT_END_DATE", request.endDate() != null ? request.endDate().toString() : "");
        autoData.put("CONTRACT_VALUE", request.value() != null ? String.format("%.2f", request.value()) : "");
        autoData.put("CONTRACT_NOTES", request.notes() != null ? request.notes() : "");

        Map<String, String> mergedMappings = new HashMap<>(request.mappings());
        autoData.forEach(mergedMappings::putIfAbsent);

        validateMandatoryFields(template, mergedMappings);

        Contract contract = generationMapper.toContractEntity(request, template);
        contract = contractRepository.save(contract);

        List<DocumentFieldValue> fieldValues = buildFieldValues(contract, template, mergedMappings);
        if (!fieldValues.isEmpty()) {
            fieldValueRepository.saveAll(fieldValues);
            contract.setFieldValues(fieldValues);
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
            contract.setDocumentContent(FileUtils.compress(pdf));
            contract.setDocumentFormat(DocumentFormat.PDF);
            contract = contractRepository.save(contract);
        } catch (IOException e) {
            throw new ContractGenerationFailException("Failed to generate contract document: " + e.getMessage());
        }

        return contractMapper.toResponseDTO(contract);
    }

    /**
     * Fetches client details from the public schema using JdbcTemplate.
     * Bridges the two isolated schemas to auto-populate contract field values.
     *
     * @param clientId the client ID in the public schema
     * @return map of field keys to their values; empty map if client not found or query fails
     */
    private Map<String, String> fetchClientData(Integer clientId) {
        String sql = "SELECT denumire as name, cui, adresa as address, tip as type, administratie as admin FROM public.\"Client\" WHERE id = ?";
        try {
            Map<String, Object> data = jdbcTemplate.queryForMap(sql, clientId);
            Map<String, String> result = new HashMap<>();
            result.put("CLIENT_NAME",    String.valueOf(data.getOrDefault("name",    "")));
            result.put("CLIENT_CUI",     String.valueOf(data.getOrDefault("cui",     "")));
            result.put("CLIENT_ADDRESS", String.valueOf(data.getOrDefault("address", "")));
            result.put("CLIENT_TYPE",    String.valueOf(data.getOrDefault("type",    "")));
            result.put("CLIENT_ADMIN",   String.valueOf(data.getOrDefault("admin",   "")));
            return result;
        } catch (Exception e) {
            log.warn("Failed to fetch client data for ID {}: {}", clientId, e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Uploads a signed contract document, converts it to PDF if necessary,
     * and transitions the contract status to ACTIVE.
     *
     * @param contractId the ID of the contract to update
     * @param fileBytes  the signed document file bytes (DOCX or PDF)
     * @return ContractResponseDTO with updated contract details
     */
    @Transactional
    public ContractResponseDTO uploadSignedContract(Long contractId, byte[] fileBytes) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found: " + contractId));

        try {
            DocumentFormat sourceFormat = Utils.detectDocumentFormat(fileBytes);
            byte[] pdfBytes = sourceFormat != DocumentFormat.PDF
                    ? FileUtils.convert(fileBytes, sourceFormat, DocumentFormat.PDF)
                    : fileBytes;

            contract.setSignedDocumentContent(FileUtils.compress(pdfBytes));
            contract.setContractStatus(ContractStatus.ACTIVE);
            contract = contractRepository.save(contract);
        } catch (IOException e) {
            throw new FileConversionException("Failed to process signed document: " + e.getMessage(), e);
        }

        return contractMapper.toResponseDTO(contract);
    }

    @Transactional
    public void terminateContract(Long contractId, @Valid ContractTerminationRequest request) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found: " + contractId));

        if (contract.getContractStatus() != ContractStatus.ACTIVE) {
            throw new InvalidContractStateException(
                    "Cannot terminate contract in status: " + contract.getContractStatus() +
                            ". Only ACTIVE contracts can be terminated."
            );
        }

        contract.setContractStatus(ContractStatus.TERMINATED);
        contract.setTerminationDate(request.getTerminationDate().toLocalDate());
        contract.setReasonsForTermination(request.getReasons());
        contractRepository.save(contract);
    }

    @Transactional(readOnly = true)
    public Page<ContractResponseDTO> getAll(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return contractRepository.findAll(pageable)
                .map(contractMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<ContractResponseDTO> search(SearchRequest request) {
        log.info("Searching contracts with criteria: {}", request);

        int pageIndex = request.page() != null ? request.page() : DEFAULT_PAGE;
        int pageSize  = request.size() != null ? request.size() : DEFAULT_PAGE_SIZE;

        PageRequest pageable = PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Contract> page = contractRepository.findAll(
                contractSpecification.buildSearchSpecification(request), pageable);

        log.debug("Search returned {}/{} contracts (page {}/{})",
                page.getNumberOfElements(), page.getTotalElements(),
                page.getNumber(), page.getTotalPages());

        return page.map(contractMapper::toResponseDTO);
    }

    private void validateMandatoryFields(DocumentTemplate template, Map<String, String> mappings) {
        List<String> missing = template.getTemplateFields().stream()
                .filter(TemplateField::getIsRequired)
                .filter(f -> f.getFieldLabel() != null)
                .filter(f -> !mappings.containsKey(f.getFieldLabel()) || mappings.get(f.getFieldLabel()).isBlank())
                .map(TemplateField::getFieldLabel)
                .toList();

        if (!missing.isEmpty()) {
            String message = "Missing mandatory field mappings: " + String.join(", ", missing);
            log.warn(message);
            throw new MissingMandatoryFieldException(message, missing);
        }
    }

    private List<DocumentFieldValue> buildFieldValues(Contract contract, DocumentTemplate template,
                                                       Map<String, String> mappings) {
        List<DocumentFieldValue> fieldValues = new ArrayList<>();
        for (TemplateField field : template.getTemplateFields()) {
            if (field.getFieldLabel() == null) continue;
            String value = mappings.get(field.getFieldLabel());
            if (value == null || value.isBlank()) continue;
            fieldValues.add(DocumentFieldValue.builder()
                    .document(contract)
                    .templateField(field)
                    .fieldValue(value)
                    .build());
        }
        return fieldValues;
    }

    private static Map<String, String> buildLabelValueMap(List<DocumentFieldValue> fieldValues) {
        Map<String, String> map = new HashMap<>(fieldValues.size() * 2);
        for (DocumentFieldValue dfv : fieldValues) {
            TemplateField field = dfv.getTemplateField();
            if (field != null && field.getFieldLabel() != null && dfv.getFieldValue() != null) {
                map.put(field.getFieldLabel(), dfv.getFieldValue());
            }
        }
        return map;
    }
}

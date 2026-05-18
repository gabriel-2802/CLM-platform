package clm.demo.services;

import clm.demo.cache.CacheNames;
import clm.demo.dto.requests.ContractTerminationRequest;
import clm.demo.dto.requests.ContractUpdateRequest;
import clm.demo.dto.requests.GenContractRequest;
import clm.demo.dto.requests.SearchRequest;
import clm.demo.dto.responses.ContractResponseDTO;
import clm.demo.exceptions.exceptions.*;
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
import clm.demo.services.utility.DocumentGenerationUtil;
import clm.demo.specifications.ContractSpecification;
import clm.demo.utils.Utils;
import clm.demo.utils.docx.DocxFiller;
import clm.demo.utils.file.FileUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static clm.demo.utils.Constants.DEFAULT_PAGE;
import static clm.demo.utils.Constants.DEFAULT_PAGE_SIZE;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class ContractService {

    private static final String SORT_FIELD_GENERATED_AT = "generatedAt";

    private final DocumentTemplateRepository   templateRepository;
    private final ContractRepository           contractRepository;
    private final DocumentFieldValueRepository fieldValueRepository;
    private final ContractGenerationMapper     generationMapper;
    private final GeneratedContractMapper      contractMapper;
    private final ContractSpecification        contractSpecification;
    private final FileUtils                    fileUtils;
    private final DocumentGenerationUtil       documentGenerationUtil;

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
    public ContractResponseDTO generateContract(GenContractRequest request) {
        DocumentTemplate template = templateRepository.findById(request.templateId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Template not found: " + request.templateId()));

        if (Boolean.FALSE.equals(template.getIsFullyMapped())) {
            throw new TemplateIncompleteException(
                    "Template " + template.getId() + " is not fully mapped.");
        }

        documentGenerationUtil.validateMandatoryFields(template, request.mappings());

        Contract contract = generationMapper.toContractEntity(request, template);
        contract = contractRepository.save(contract);

        List<DocumentFieldValue> fieldValues =
                documentGenerationUtil.buildFieldValues(contract, template, request.mappings());

        if (!fieldValues.isEmpty()) {
            fieldValueRepository.saveAll(fieldValues);
            contract.setFieldValues(fieldValues);
        }

        try {
            contract = fillAndPersistDocument(contract, template, fieldValues);
        } catch (IOException e) {
            throw new ContractGenerationFailException(
                    "Failed to generate contract document: " + e.getMessage());
        }

        return contractMapper.toResponseDTO(contract);
    }

    /**
     * Uploads a signed contract document, converts it to PDF if necessary,
     * and transitions the contract status to ACTIVE.
     *
     * @param contractId the ID of the contract to update
     * @param fileBytes  the signed document file bytes (DOCX or PDF)
     * @return ContractResponseDTO with updated contract details
     */
    @CacheEvict(value = CacheNames.CONTRACTS, key = "#contractId")
    @Transactional
    public ContractResponseDTO uploadSignedContract(Long contractId, byte[] fileBytes, Integer userId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Contract not found: " + contractId));

        try {
            DocumentFormat sourceFormat = Utils.detectDocumentFormat(fileBytes);
            byte[] pdfBytes = sourceFormat != DocumentFormat.PDF
                    ? fileUtils.convert(fileBytes, sourceFormat, DocumentFormat.PDF)
                    : fileBytes;

            contract.setSignedDocumentContent(fileUtils.compress(pdfBytes));
            contract.setContractStatus(ContractStatus.ACTIVE);
            contract.setUploadedSignedAt(LocalDateTime.now());
            contract.setUploadedSignedByUser(userId);
            contract = contractRepository.save(contract);
        } catch (IOException e) {
            throw new FileConversionException(
                    "Failed to process signed document: " + e.getMessage(), e);
        }

        return contractMapper.toResponseDTO(contract);
    }

    @CacheEvict(value = CacheNames.CONTRACTS, key = "#contractId")
    @Transactional
    public void terminateContract(Long contractId, @Valid ContractTerminationRequest request) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Contract not found: " + contractId));

        if (contract.getContractStatus() != ContractStatus.ACTIVE) {
            throw new InvalidContractStateException(
                    "Cannot terminate contract in status: " + contract.getContractStatus()
                            + ". Only ACTIVE contracts can be terminated.");
        }

        contract.setContractStatus(ContractStatus.TERMINATED);
        contract.setTerminatedByUserId(request.getUserId());
        contract.setTerminatedAt(LocalDateTime.now());
        contract.setTerminationDate(request.getTerminationDate());
        contract.setReasonsForTermination(request.getReasons());
        contractRepository.save(contract);
    }

    /**
     * Toggles the auto-renewal flag for a contract.
     * If autoRenew is false, it will be set to true and vice versa.
     *
     * @param contractId the ID of the contract to update
     * @return ContractResponseDTO with updated auto-renewal status
     * @throws ResourceNotFoundException if contract is not found
     */
    @CacheEvict(value = CacheNames.CONTRACTS, key = "#contractId")
    @Transactional
    public ContractResponseDTO toggleAutoRenewal(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Contract not found: " + contractId));

        contract.setAutoRenew(!contract.getAutoRenew());
        contract = contractRepository.save(contract);
        log.info("Auto-renewal toggled for contract {}: new state = {}",
                contractId, contract.getAutoRenew());

        return contractMapper.toResponseDTO(contract);
    }

    @Cacheable(value = CacheNames.CONTRACTS, key = "#contractId")
    @Transactional(readOnly = true)
    public ContractResponseDTO getById(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found: " + contractId));
        return contractMapper.toResponseDTO(contract);
    }

    @CacheEvict(value = CacheNames.CONTRACTS, key = "#contractId")
    @Transactional
    public ContractResponseDTO renegotiateContract(Long contractId,
                                                   clm.demo.dto.requests.RenegotiateContractRequest request) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found: " + contractId));

        if (contract.getContractStatus() != ContractStatus.ACTIVE) {
            throw new InvalidContractStateException(
                    "Cannot renegotiate contract in status: " + contract.getContractStatus()
                            + ". Only ACTIVE contracts can be renegotiated.");
        }

        if (request.getContractValue() != null)  contract.setContractValue(request.getContractValue());
        if (request.getContractEndDate() != null) contract.setContractEndDate(request.getContractEndDate());

        contract = contractRepository.save(contract);
        log.info("Contract {} renegotiated — value={}, endDate={}", contractId,
                contract.getContractValue(), contract.getContractEndDate());
        return contractMapper.toResponseDTO(contract);
    }

    @Transactional(readOnly = true)
    public Page<ContractResponseDTO> getAll(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, SORT_FIELD_GENERATED_AT));
        return contractRepository.findAll(pageable)
                .map(contractMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<ContractResponseDTO> search(SearchRequest request) {
        log.info("Searching contracts with criteria: {}", request);

        int pageIndex = Objects.nonNull(request.page()) ? request.page() : DEFAULT_PAGE;
        int pageSize  = Objects.nonNull(request.size()) ? request.size() : DEFAULT_PAGE_SIZE;

        PageRequest pageable = PageRequest.of(pageIndex, pageSize,
                Sort.by(Sort.Direction.DESC, SORT_FIELD_GENERATED_AT));

        Page<Contract> result = contractRepository.findAll(
                contractSpecification.buildSearchSpecification(request), pageable);

        log.debug("Search returned {}/{} contracts (page {}/{})",
                result.getNumberOfElements(), result.getTotalElements(),
                result.getNumber(), result.getTotalPages());

        return result.map(contractMapper::toResponseDTO);
    }

    @CacheEvict(value = CacheNames.CONTRACTS, key = "#contractId")
    @Transactional
    public ContractResponseDTO updateContractTerms(Long contractId, @Valid ContractUpdateRequest request) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Contract not found: " + contractId));

        if (Objects.nonNull(request.contractEndDate())) {
            contract.setContractEndDate(request.contractEndDate());
        }
        if (Objects.nonNull(request.balance())) {
            contract.setContractBalance(request.balance());
        }
        if (Objects.nonNull(request.value())) {
            contract.setContractValue(request.value());
        }

        contract.setModifiedAt(LocalDateTime.now());
        contract.setModifiedByUserId(request.userId());

        log.info("Contract {} terms updated by user {}: endDate={}, balance={}, value={}",
                contractId, request.userId(),
                request.contractEndDate(), request.balance(), request.value());

        return contractMapper.toResponseDTO(contractRepository.save(contract));
    }

    private Contract fillAndPersistDocument(
            Contract contract,
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

        contract.setDocumentContent(fileUtils.compress(pdf));
        contract.setDocumentFormat(DocumentFormat.PDF);
        return contractRepository.save(contract);
    }
}
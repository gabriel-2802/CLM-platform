package clm.demo.services;

import clm.demo.cache.CacheNames;
import clm.demo.dto.requests.ContractTerminationRequest;
import clm.demo.dto.requests.ContractUpdateRequest;
import clm.demo.dto.requests.GenContractRequest;
import clm.demo.dto.requests.SearchRequest;
import clm.demo.dto.responses.ContractResponseDTO;
import clm.demo.dto.responses.DetailedContractResponseDTO;
import clm.demo.exceptions.exceptions.*;
import clm.demo.mappers.ContractDetailsMapper;
import clm.demo.mappers.ContractGenerationMapper;
import clm.demo.mappers.GeneratedContractMapper;
import clm.demo.models.Appendix;
import clm.demo.models.Contract;
import clm.demo.models.ContractDetails;
import clm.demo.models.DocumentFieldValue;
import clm.demo.models.DocumentTemplate;
import clm.demo.models.TemplateField;
import clm.demo.models.enums.ContractStatus;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.repositories.AppendixRepository;
import clm.demo.repositories.ContractDetailsRepository;
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
import java.time.LocalDate;
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
    private final ContractDetailsRepository    contractDetailsRepository;
    private final AppendixRepository           appendixRepository;
    private final DocumentFieldValueRepository fieldValueRepository;
    private final ContractGenerationMapper     generationMapper;
    private final GeneratedContractMapper      contractMapper;
    private final ContractDetailsMapper        contractDetailsMapper;
    private final ContractSpecification        contractSpecification;
    private final FileUtils                    fileUtils;
    private final DocumentGenerationUtil       documentGenerationUtil;

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

        ContractDetails initialDetails = ContractDetails.builder()
                .contract(contract)
                .contractValue(request.value())
                .contractBalance(request.contractBalance())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .createdAt(LocalDateTime.now())
                .createdByUserId(request.userId().intValue())
                .appendix(null)
                .build();
        contractDetailsRepository.save(initialDetails);
        contract.getContractDetailsList().add(initialDetails);

        contract.setStartDate(request.startDate());
        contract = contractRepository.save(contract);

        return contractMapper.toResponseDTO(contract, getCurentlyActiveContractDetails(contract.getContractDetailsList()));
    }

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

        return contractMapper.toResponseDTO(contract, getCurentlyActiveContractDetails(contract.getContractDetailsList()));
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

        contract.setContractStatus(ContractStatus.TERMINATION_DUE);
        contract.setTerminatedByUserId(request.getUserId());
        contract.setTerminatedAt(LocalDateTime.now());
        contract.setTerminationDate(request.getTerminationDate());
        contract.setReasonsForTermination(request.getReasons());
        contractRepository.save(contract);
    }


    @Cacheable(value = CacheNames.CONTRACTS, key = "#contractId")
    @Transactional(readOnly = true)
    public ContractResponseDTO getById(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found: " + contractId));
        return contractMapper.toResponseDTO(contract, getCurentlyActiveContractDetails(contract.getContractDetailsList()));
    }

    @Transactional(readOnly = true)
    public DetailedContractResponseDTO getDetailedById(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found: " + contractId));
        return contractDetailsMapper.toDetailedResponseDTO(contract);
    }


    @Transactional(readOnly = true)
    public Page<ContractResponseDTO> getAll(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, SORT_FIELD_GENERATED_AT));
        return contractRepository.findAll(pageable)
                .map(c -> contractMapper.toResponseDTO(c, getCurentlyActiveContractDetails(c.getContractDetailsList())));
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

        return result.map( c -> contractMapper.toResponseDTO(c, getCurentlyActiveContractDetails(c.getContractDetailsList())));
    }

    @CacheEvict(value = CacheNames.CONTRACTS, key = "#contractId")
    @Transactional
    public ContractResponseDTO updateContractTerms(Long contractId, ContractUpdateRequest request) {

        if (Objects.isNull(request.value()) && Objects.isNull(request.balance()) && Objects.isNull(request.contractEndDate())) {
            throw new InvalidContractUpdateException(
                    "At least one of value, balance, or contractEndDate must be provided for update.");
        }

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Contract not found: " + contractId));

        if (contract.getContractStatus() != ContractStatus.ACTIVE) {
            throw new InvalidContractStateException(
                    "Cannot update contract terms in status: " + contract.getContractStatus()
                            + ". Only ACTIVE contracts can be updated.");
        }

        Appendix appendix = appendixRepository.findById(Long.valueOf(request.appendixId()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appendix not found: " + request.appendixId()));

        if (contract.getContractDetailsList().stream()
                .map(ContractDetails::getAppendix)
                .filter(Objects::nonNull)
                .anyMatch(a -> a.getId().equals(appendix.getId()))) {
            throw new InvalidContractUpdateException(
                    "An Appendix has already modified this contract: " + contract.getId());
        }

        ContractDetails currentDetails = getCurentlyActiveContractDetails(contract.getContractDetailsList());

        if(request.startDate().isBefore(currentDetails.getStartDate())) {
            throw new InvalidContractUpdateException(
                    "New start date cannot be before current start date: " + currentDetails.getStartDate());
        }

        LocalDate endDate = request.contractEndDate() != null
                ? request.contractEndDate()
                : currentDetails.getEndDate();



        ContractDetails updated = ContractDetails.builder()
                .contract(contract)
                .contractValue(request.value() != null
                        ? request.value()
                        : currentDetails.getContractValue())
                .contractBalance(request.balance() != null
                        ? request.balance()
                        : currentDetails.getContractBalance())
                .startDate(request.startDate())
                .endDate(endDate)
                .createdAt(LocalDateTime.now())
                .createdByUserId(request.userId())
                .appendix(appendix)
                .build();

        contractDetailsRepository.save(updated);
        contract.getContractDetailsList().add(updated);

        log.info("Contract {} terms updated by user {}: endDate={}, balance={}, value={}",
                contractId, request.userId(),
                updated.getEndDate(), updated.getContractBalance(), updated.getContractValue());

        return contractMapper.toResponseDTO(contract, getCurentlyActiveContractDetails(contract.getContractDetailsList()));
    }

    public static ContractDetails getCurentlyActiveContractDetails(List<ContractDetails> details) {
        if (details == null || details.isEmpty()) {
            throw new IllegalStateException("All Contracts must have at least one ContractDetails.");
        }
        if (details.size() == 1) {
            return details.getFirst();
        }

        return details.stream()
                .filter(d -> !d.getStartDate().isAfter(LocalDate.now()) && !d.getEndDate().isBefore(LocalDate.now()))
                .max(Comparator.comparing(ContractDetails::getCreatedAt))
                .orElseThrow(() -> new IllegalStateException("No active ContractDetails found."));
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

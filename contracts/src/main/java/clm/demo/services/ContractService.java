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
import clm.demo.utils.Constants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private final FileUtils fileUtils;
    private final DocumentGenerationUtil documentGenerationUtil;

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
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + request.templateId()));

        if (!template.getIsFullyMapped()) {
            throw new TemplateIncompleteException("Template " + template.getId() + " is not fully mapped.");
        }

        documentGenerationUtil.validateMandatoryFields(template, request.mappings());

        Contract contract = generationMapper.toContractEntity(request, template);
        contract = contractRepository.save(contract);

        List<DocumentFieldValue> fieldValues = documentGenerationUtil.buildFieldValues(contract, template, request.mappings());
        if (!fieldValues.isEmpty()) {
            fieldValueRepository.saveAll(fieldValues);
            contract.setFieldValues(fieldValues);
        }

        try {
            List<TemplateField> ordered = template.getTemplateFields().stream()
                    .filter(f -> f.getFieldPosition() != null && f.getFieldLabel() != null)
                    .sorted(java.util.Comparator.comparingInt(TemplateField::getFieldPosition))
                    .toList();
            Map<String, String> labelToValue = documentGenerationUtil.buildLabelValueMap(fieldValues);
            byte[] templateBytes = fileUtils.decompress(template.getDocumentContent());
            byte[] filled = DocxFiller.fillDocx(templateBytes, ordered, labelToValue);
            byte[] pdf = fileUtils.convert(filled, DocumentFormat.DOCX, DocumentFormat.PDF);
            contract.setDocumentContent(fileUtils.compress(pdf));
            contract.setDocumentFormat(DocumentFormat.PDF);
            contract = contractRepository.save(contract);
        } catch (IOException e) {
            throw new ContractGenerationFailException("Failed to generate contract document: " + e.getMessage());
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
    @Transactional
    public ContractResponseDTO uploadSignedContract(Long contractId, byte[] fileBytes) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found: " + contractId));

        try {
            DocumentFormat sourceFormat = Utils.detectDocumentFormat(fileBytes);
            byte[] pdfBytes = sourceFormat != DocumentFormat.PDF
                    ? fileUtils.convert(fileBytes, sourceFormat, DocumentFormat.PDF)
                    : fileBytes;

            contract.setSignedDocumentContent(fileUtils.compress(pdfBytes));
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
}

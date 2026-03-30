package clm.demo.services;

import clm.demo.dto.requests.GenContractRequest;
import clm.demo.dto.responses.ContractResponseDTO;
import clm.demo.exceptions.MissingMandatoryFieldException;
import clm.demo.exceptions.ResourceNotFoundException;
import clm.demo.exceptions.TemplateIncompleteException;
import clm.demo.mappers.ContractGenerationMapper;
import clm.demo.mappers.GeneratedContractMapper;
import clm.demo.models.Contract;
import clm.demo.models.ContractFieldValue;
import clm.demo.models.Template;
import clm.demo.models.TemplateField;
import clm.demo.repositories.ContractFieldValueRepository;
import clm.demo.repositories.ContractTemplateRepository;
import clm.demo.repositories.GeneratedContractRepository;
import clm.demo.services.file.actions.FileContentReplacementService;
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

    private final ContractTemplateRepository contractTemplateRepository;
    private final GeneratedContractRepository generatedContractRepository;
    private final ContractFieldValueRepository contractFieldValueRepository;
    private final ContractGenerationMapper contractGenerationMapper;
    private final GeneratedContractMapper generatedContractMapper;
    private final FileContentReplacementService fileContentReplacementService;
    private final FileZipService zipService;

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

    // ...existing code...
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
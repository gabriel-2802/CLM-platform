package clm.demo.services.download.document.providers;

import clm.demo.exceptions.ResourceNotFoundException;
import clm.demo.models.Contract;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.models.enums.DocumentType;
import clm.demo.repositories.ContractRepository;
import clm.demo.services.download.DocumentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Document provider for generated contracts.
 * Contracts are only stored in — and downloadable as — PDF format.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UnsignedContractProvider implements DocumentProvider {

    private final ContractRepository contractRepository;

    /**
     * Fetches the contract in a single repository call and returns both the
     * compressed content and its native format (always PDF) together.
     */
    @Override
    public DocumentResult getDocument(Long documentId) {
        Contract contract = contractRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found: " + documentId));

        if (contract.getDocumentContent() == null) {
            throw new ResourceNotFoundException("Contract document not yet available: " + documentId);
        }

        return new DocumentResult(contract.getDocumentContent(), DocumentFormat.PDF);
    }

    @Override
    public boolean supportsFormat(DocumentFormat targetFormat) {
        // contracts are always stored as PDF and can only be downloaded as PDF
        return targetFormat == DocumentFormat.PDF || targetFormat == DocumentFormat.DOCX;
    }

    @Override
    public DocumentType getDocumentType() {
        return DocumentType.UNSIGNED_CONTRACT;
    }
}
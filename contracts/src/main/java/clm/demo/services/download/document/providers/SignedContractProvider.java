package clm.demo.services.download.document.providers;

import clm.demo.exceptions.ResourceNotFoundException;
import clm.demo.exceptions.SignedDocumentNotAvailableException;
import clm.demo.models.Contract;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.models.enums.DocumentType;
import clm.demo.repositories.ContractRepository;
import clm.demo.services.download.DocumentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Document provider for signed contracts.
 * Signed contracts are only downloadable as PDF format.
 * A signed document must be available; if not, a SignedDocumentNotAvailableException is thrown.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SignedContractProvider implements DocumentProvider {

    private final ContractRepository contractRepository;

    /**
     * Fetches the signed contract in a single repository call and returns both the
     * compressed content and its native format (always PDF) together.
     *
     * @param documentId the contract ID to retrieve
     * @return DocumentResult containing the signed document bytes and PDF format
     * @throws ResourceNotFoundException if the contract is not found
     * @throws SignedDocumentNotAvailableException if the signed document is null
     */
    @Override
    public DocumentResult getDocument(Long documentId) {
        Contract contract = contractRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found: " + documentId));

        if (contract.getSignedDocument() == null) {
            throw new SignedDocumentNotAvailableException(documentId);
        }

        return new DocumentResult(contract.getSignedDocument(), DocumentFormat.PDF);
    }

    /**
     * Validates that the requested format is PDF.
     * Signed contracts can only be downloaded as PDF format.
     *
     * @param targetFormat the requested output format
     * @return true only if targetFormat is PDF
     */
    @Override
    public boolean supportsFormat(DocumentFormat targetFormat) {
        return targetFormat == DocumentFormat.PDF;
    }

    /**
     * Returns the document type this provider handles.
     *
     * @return DocumentType.SIGNED_CONTRACT
     */
    @Override
    public DocumentType getDocumentType() {
        return DocumentType.SIGNED_CONTRACT;
    }
}


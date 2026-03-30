package clm.demo.services.download;

import clm.demo.exceptions.ResourceNotFoundException;
import clm.demo.models.Contract;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.models.enums.DocumentType;
import clm.demo.repositories.GeneratedContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Document provider for generated contracts.
 * Contracts are only stored in PDF format.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContractDocumentProvider implements DocumentProvider {

    private final GeneratedContractRepository contractRepository;

    @Override
    public byte[] getCompressedDocument(Long documentId) {
        log.debug("Fetching compressed contract document: {}", documentId);
        
        Contract contract = contractRepository.findById(documentId)
                .orElseThrow(() -> {
                    log.warn("Contract not found: {}", documentId);
                    return new ResourceNotFoundException("Contract not found: " + documentId);
                });

        if (contract.getDocumentContent() == null) {
            log.warn("Contract {} has no document content", documentId);
            throw new ResourceNotFoundException("Contract document not available: " + documentId);
        }

        return contract.getDocumentContent();
    }

    @Override
    public DocumentFormat getNativeFormat(Long documentId) {
        // contracts are always stored as PDF
        return DocumentFormat.PDF;
    }

    @Override
    public boolean supportsFormat(DocumentFormat targetFormat) {
        // contracts only support PDF format
        return targetFormat == DocumentFormat.PDF;
    }

    @Override
    public DocumentType getDocumentType() {
        return DocumentType.CONTRACT;
    }
}


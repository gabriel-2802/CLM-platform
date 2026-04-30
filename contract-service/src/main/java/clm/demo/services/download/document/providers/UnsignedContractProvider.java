package clm.demo.services.download.document.providers;

import clm.demo.exceptions.exceptions.ResourceNotFoundException;
import clm.demo.models.Contract;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.models.enums.DocumentType;
import clm.demo.repositories.ContractRepository;
import clm.demo.services.download.DocumentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UnsignedContractProvider implements DocumentProvider {

    private final ContractRepository contractRepository;

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
        return targetFormat == DocumentFormat.PDF || targetFormat == DocumentFormat.DOCX;
    }

    @Override
    public DocumentType getDocumentType() {
        return DocumentType.UNSIGNED_CONTRACT;
    }
}

package clm.demo.services.download.document.providers;

import clm.demo.exceptions.exceptions.ResourceNotFoundException;
import clm.demo.exceptions.exceptions.SignedDocumentNotAvailableException;
import clm.demo.models.Appendix;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.models.enums.DocumentType;
import clm.demo.repositories.AppendixRepository;
import clm.demo.services.download.DocumentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SignedAppendixProvider implements DocumentProvider {

    private final AppendixRepository appendixRepository;

    @Override
    public DocumentResult getDocument(Long documentId) {
        Appendix appendix = appendixRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appendix not found: " + documentId));

        if (appendix.getSignedDocumentContent() == null) {
            throw new SignedDocumentNotAvailableException(documentId);
        }

        return new DocumentResult(appendix.getSignedDocumentContent(), DocumentFormat.PDF);
    }

    @Override
    public boolean supportsFormat(DocumentFormat targetFormat) {
        return targetFormat == DocumentFormat.PDF;
    }

    @Override
    public DocumentType getDocumentType() {
        return DocumentType.SIGNED_APPENDIX;
    }
}

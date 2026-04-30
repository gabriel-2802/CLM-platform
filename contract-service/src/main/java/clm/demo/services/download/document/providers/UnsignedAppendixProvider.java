package clm.demo.services.download.document.providers;

import clm.demo.exceptions.exceptions.ResourceNotFoundException;
import clm.demo.models.Appendix;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.models.enums.DocumentType;
import clm.demo.repositories.AppendixRepository;
import clm.demo.services.download.DocumentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UnsignedAppendixProvider implements DocumentProvider {

    private final AppendixRepository appendixRepository;

    @Override
    public DocumentResult getDocument(Long documentId) {
        Appendix appendix = appendixRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appendix not found: " + documentId));

        if (appendix.getDocumentContent() == null) {
            throw new ResourceNotFoundException("Appendix document not yet available: " + documentId);
        }

        // nativeFormat may be null for non-fillable appendices stored before format detection;
        // fall back to PDF as the safe default.
        DocumentFormat format = appendix.getDocumentFormat() != null
                ? appendix.getDocumentFormat()
                : DocumentFormat.PDF;

        return new DocumentResult(appendix.getDocumentContent(), format);
    }

    @Override
    public boolean supportsFormat(DocumentFormat targetFormat) {
        return targetFormat == DocumentFormat.PDF || targetFormat == DocumentFormat.DOCX;
    }

    @Override
    public DocumentType getDocumentType() {
        return DocumentType.UNSIGNED_APPENDIX;
    }
}

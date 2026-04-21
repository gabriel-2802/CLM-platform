package clm.demo.services.download.document.providers;

import clm.demo.exceptions.ResourceNotFoundException;
import clm.demo.models.DocumentTemplate;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.models.enums.DocumentType;
import clm.demo.repositories.DocumentTemplateRepository;
import clm.demo.services.download.DocumentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TemplateProvider implements DocumentProvider {

    private final DocumentTemplateRepository templateRepository;

    @Override
    public DocumentResult getDocument(Long documentId) {
        DocumentTemplate template = templateRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + documentId));
        return new DocumentResult(template.getDocumentContent(), template.getDocumentFormat());
    }

    @Override
    public boolean supportsFormat(DocumentFormat targetFormat) {
        return targetFormat == DocumentFormat.DOCX || targetFormat == DocumentFormat.PDF;
    }

    @Override
    public DocumentType getDocumentType() {
        return DocumentType.TEMPLATE;
    }
}

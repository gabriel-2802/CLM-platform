package clm.demo.services.download;

import clm.demo.exceptions.ResourceNotFoundException;
import clm.demo.models.Template;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.models.enums.DocumentType;
import clm.demo.repositories.ContractTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Document provider for contract templates.
 * Templates can be stored in both DOCX and PDF formats.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TemplateDocumentProvider implements DocumentProvider {

    private final ContractTemplateRepository templateRepository;

    /**
     * Fetches the template in a single repository call and returns both the
     * compressed content and its native format together.
     */
    @Override
    public DocumentResult getDocument(Long documentId) {
        Template template = templateRepository.findById(documentId)
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
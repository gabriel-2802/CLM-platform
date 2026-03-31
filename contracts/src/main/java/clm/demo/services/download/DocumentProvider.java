package clm.demo.services.download;

import clm.demo.models.enums.DocumentFormat;
import clm.demo.models.enums.DocumentType;

/**
 * Strategy interface for retrieving documents from different sources.
 * Implementations handle the logic of fetching and processing documents
 * based on the entity type (Template, Contract, etc.).
 */
public interface DocumentProvider {

    /**
     * Retrieves the document content together with its native format in a single
     * call, avoiding multiple round-trips to the repository.
     * The content inside {@link DocumentResult} is expected to be compressed (e.g. GZIP).
     *
     * @param documentId the ID of the document to retrieve
     * @return a {@link DocumentResult} containing the compressed bytes and native format
     */
    DocumentResult getDocument(Long documentId);

    /**
     * Checks if the document supports a given format for download.
     * Templates support both DOCX and PDF, while Contracts only support PDF.
     *
     * @param targetFormat the format to check
     * @return true if the format is supported, false otherwise
     */
    boolean supportsFormat(DocumentFormat targetFormat);

    /**
     * Returns the type of document this provider handles (e.g., TEMPLATE, CONTRACT).
     * Used for logging and error messages.
     *
     * @return the document type
     */
    DocumentType getDocumentType();
}
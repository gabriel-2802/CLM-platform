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
     * Retrieves the raw document content in its native format.
     * The content is expected to be compressed (e.g., GZIP).
     *
     * @param documentId the ID of the document to retrieve
     * @return compressed document bytes
     */
    byte[] getCompressedDocument(Long documentId);

    /**
     * Retrieves the native format of the stored document.
     *
     * @param documentId the ID of the document
     * @return the DocumentFormat of the stored document
     */
    DocumentFormat getNativeFormat(Long documentId);

    /**
     * Checks if the document supports a given format for download.
     * Templates support both DOCX and PDF, while Contracts only support PDF.
     *
     * @param targetFormat the format to check
     * @return true if the format is supported, false otherwise
     */
    boolean supportsFormat(DocumentFormat targetFormat);

    /**
     * Returns the type of document this provider handles (e.g., "Template", "Contract").
     * Used for logging and error messages.
     *
     * @return the document type name
     */
    DocumentType getDocumentType();
}


package clm.demo.services.download;

import clm.demo.models.enums.DocumentFormat;
import clm.demo.models.enums.DocumentType;
import clm.demo.services.file.actions.FileConverterService;
import clm.demo.services.file.actions.FileZipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

/**
 * Service for downloading documents in various formats.
 * Handles both templates and contracts with their respective format restrictions.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DocumentDownloadService {

    private final FileZipService zipService;
    private final FileConverterService converterService;
    private final TemplateDocumentProvider templateProvider;
    private final ContractDocumentProvider contractProvider;

    /**
     * Downloads a template in the requested format.
     * Templates support both DOCX and PDF formats.
     *
     * @param templateId   the template ID
     * @param targetFormat the desired output format (DOCX or PDF)
     * @return decompressed and possibly converted document bytes
     * @throws IllegalArgumentException if the format is unsupported
     * @throws IOException              if decompression or conversion fails
     */
    public byte[] downloadTemplate(Long templateId, DocumentFormat targetFormat) throws IOException {
        return downloadDocument(templateId, targetFormat, templateProvider);
    }

    /**
     * Downloads a contract in PDF format (the only supported format for contracts).
     *
     * @param contractId the contract ID
     * @return decompressed PDF document bytes
     * @throws IOException if decompression fails
     */
    public byte[] downloadContract(Long contractId) throws IOException {
        return downloadDocument(contractId, DocumentFormat.PDF, contractProvider);
    }

    /**
     * Generic download method that works with any document provider.
     * Retrieves the document in a single provider call, then handles
     * decompression and format conversion as needed.
     *
     * @param documentId   the document ID
     * @param targetFormat the desired output format
     * @param provider     the document provider to use
     * @return decompressed and possibly converted document bytes
     * @throws IllegalArgumentException if the format is unsupported
     * @throws IOException              if decompression or conversion fails
     */
    private byte[] downloadDocument(Long documentId, DocumentFormat targetFormat, DocumentProvider provider) throws IOException {
        DocumentType docType = provider.getDocumentType();

        if (!provider.supportsFormat(targetFormat)) {
            throw new IllegalArgumentException(docType + " does not support format: " + targetFormat);
        }

        // returns both compressed bytes and native format
        DocumentResult result = provider.getDocument(documentId);

        byte[] decompressedContent = zipService.decompress(result.compressedContent());

        // if already in target format, return directly — no conversion needed
        if (result.nativeFormat() == targetFormat) {
            return decompressedContent;
        }

        return converterService.convert(decompressedContent, result.nativeFormat(), targetFormat);
    }

    /**
     * Gets the content type (MIME type) for a given document format.
     * Useful for setting HTTP response headers.
     *
     * @param format the document format
     * @return the MIME type string
     */
    public String getContentType(DocumentFormat format) {
        return switch (format) {
            case PDF -> "application/pdf";
            case DOCX -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        };
    }

    /**
     * Gets the file extension for a given document format.
     *
     * @param format the document format
     * @return the file extension (without the dot)
     */
    public String getFileExtension(DocumentFormat format) {
        return switch (format) {
            case PDF -> "pdf";
            case DOCX -> "docx";
        };
    }
}
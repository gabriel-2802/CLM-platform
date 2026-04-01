package clm.demo.services.download;

import clm.demo.exceptions.FileConversionException;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.models.enums.DocumentType;
import clm.demo.services.download.document.providers.DocumentProvider;
import clm.demo.utils.file.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Service for downloading documents in various formats.
 * Delegates document retrieval to the appropriate {@link DocumentProvider}
 * via {@link DocumentProviderRegistry} — no concrete provider types are
 * referenced here, so adding a new document type requires no changes to
 * this class.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentDownloadService {
    private final DocumentProviderRegistry providerRegistry;

    /**
     * Resolves the correct provider from the registry, decompresses the stored
     * document, and converts to the target format if necessary.
     *
     * <p>New document types are supported by registering a provider in
     * {@link DocumentProviderRegistry} — no changes to this class are required.</p>
     *
     * @param documentId   the document ID
     * @param targetFormat the desired output format
     * @param documentType the type of document to download
     * @return decompressed and converted document bytes
     * @throws IllegalArgumentException if the format is unsupported for this document type
     * @throws FileConversionException  if decompression or conversion fails
     */
    public byte[] downloadDocument(Long documentId, DocumentFormat targetFormat, DocumentType documentType) {
        DocumentProvider provider = providerRegistry.getProvider(documentType);

        if (!provider.supportsFormat(targetFormat)) {
            throw new IllegalArgumentException(documentType + " does not support format: " + targetFormat);
        }

        try {
            DocumentResult result = provider.getDocument(documentId);
            byte[] decompressed = FileUtils.decompress(result.compressedContent());

            if (result.nativeFormat() == targetFormat) {
                return decompressed;
            }

            return FileUtils.convert(decompressed, result.nativeFormat(), targetFormat);

        } catch (IOException e) {
            throw new FileConversionException("Failed to download document " + documentId + ": " + e.getMessage(), e);
        }
    }
}
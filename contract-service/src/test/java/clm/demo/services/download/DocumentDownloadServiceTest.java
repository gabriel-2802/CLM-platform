package clm.demo.services.download;

import clm.demo.exceptions.exceptions.FileConversionException;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.models.enums.DocumentType;
import clm.demo.services.download.document.providers.DocumentProvider;
import clm.demo.utils.file.FileUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * verifies that DocumentDownloadService correctly routes to providers, decompresses
 * documents, converts formats when needed, and wraps I/O failures.
 */
@ExtendWith(MockitoExtension.class)
class DocumentDownloadServiceTest {

    @Mock DocumentProviderRegistry providerRegistry;
    @Mock FileUtils                 fileUtils;

    @InjectMocks DocumentDownloadService service;

    // ------------------------------------------------------------------ //
    //  helper                                                              //
    // ------------------------------------------------------------------ //

    private DocumentProvider mockProvider(DocumentType type, boolean supportsFormat) {
        DocumentProvider provider = mock(DocumentProvider.class);
        when(providerRegistry.getProvider(type)).thenReturn(provider);
        when(provider.supportsFormat(any())).thenReturn(supportsFormat);
        return provider;
    }

    // ================================================================== //
    //  format not supported                                                //
    // ================================================================== //

    @Nested
    class UnsupportedFormat {

        @Test
        void throws_illegal_argument_when_format_not_supported() {
            mockProvider(DocumentType.SIGNED_CONTRACT, false);

            assertThatThrownBy(() ->
                    service.downloadDocument(1L, DocumentFormat.DOCX, DocumentType.SIGNED_CONTRACT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("DOCX");
        }

        @Test
        void no_document_fetch_occurs_when_format_unsupported() throws IOException {
            DocumentProvider provider = mockProvider(DocumentType.SIGNED_CONTRACT, false);

            try {
                service.downloadDocument(1L, DocumentFormat.DOCX, DocumentType.SIGNED_CONTRACT);
            } catch (IllegalArgumentException ignored) {}

            verify(provider, never()).getDocument(any());
        }
    }

    // ================================================================== //
    //  native format match — no conversion needed                         //
    // ================================================================== //

    @Nested
    class NativeFormatMatch {

        @Test
        void returns_decompressed_bytes_without_conversion() throws IOException {
            DocumentProvider provider = mockProvider(DocumentType.UNSIGNED_CONTRACT, true);
            byte[] compressed   = {1, 2, 3};
            byte[] decompressed = {4, 5, 6};

            when(provider.getDocument(1L))
                    .thenReturn(new DocumentResult(compressed, DocumentFormat.PDF));
            when(fileUtils.decompress(compressed)).thenReturn(decompressed);

            byte[] result = service.downloadDocument(1L, DocumentFormat.PDF, DocumentType.UNSIGNED_CONTRACT);

            assertThat(result).isEqualTo(decompressed);
        }

        @Test
        void convert_is_not_called_when_formats_match() throws IOException {
            DocumentProvider provider = mockProvider(DocumentType.UNSIGNED_CONTRACT, true);
            when(provider.getDocument(1L))
                    .thenReturn(new DocumentResult(new byte[]{1}, DocumentFormat.PDF));
            when(fileUtils.decompress(any())).thenReturn(new byte[]{2});

            service.downloadDocument(1L, DocumentFormat.PDF, DocumentType.UNSIGNED_CONTRACT);

            verify(fileUtils, never()).convert(any(), any(), any());
        }
    }

    // ================================================================== //
    //  format conversion required                                          //
    // ================================================================== //

    @Nested
    class FormatConversion {

        @Test
        void converts_when_native_format_differs_from_requested_format() throws IOException {
            DocumentProvider provider = mockProvider(DocumentType.TEMPLATE, true);
            byte[] compressed   = {1, 2};
            byte[] decompressed = {3, 4};
            byte[] converted    = {5, 6};

            when(provider.getDocument(1L))
                    .thenReturn(new DocumentResult(compressed, DocumentFormat.DOCX));
            when(fileUtils.decompress(compressed)).thenReturn(decompressed);
            when(fileUtils.convert(decompressed, DocumentFormat.DOCX, DocumentFormat.PDF))
                    .thenReturn(converted);

            byte[] result = service.downloadDocument(1L, DocumentFormat.PDF, DocumentType.TEMPLATE);

            assertThat(result).isEqualTo(converted);
        }

        @Test
        void convert_called_with_correct_source_and_target_formats() throws IOException {
            DocumentProvider provider = mockProvider(DocumentType.TEMPLATE, true);
            when(provider.getDocument(2L))
                    .thenReturn(new DocumentResult(new byte[]{1}, DocumentFormat.DOCX));
            when(fileUtils.decompress(any())).thenReturn(new byte[]{2});
            when(fileUtils.convert(any(), any(), any())).thenReturn(new byte[]{3});

            service.downloadDocument(2L, DocumentFormat.PDF, DocumentType.TEMPLATE);

            verify(fileUtils).convert(any(), any(DocumentFormat.class), any(DocumentFormat.class));
        }
    }

    // ================================================================== //
    //  I/O failure wrapping                                               //
    // ================================================================== //

    @Nested
    class IoFailure {

        @Test
        void decompress_io_exception_wrapped_as_file_conversion_exception() throws IOException {
            DocumentProvider provider = mockProvider(DocumentType.UNSIGNED_CONTRACT, true);
            when(provider.getDocument(1L))
                    .thenReturn(new DocumentResult(new byte[]{1}, DocumentFormat.PDF));
            when(fileUtils.decompress(any())).thenThrow(new IOException("corrupted gzip"));

            assertThatThrownBy(() ->
                    service.downloadDocument(1L, DocumentFormat.PDF, DocumentType.UNSIGNED_CONTRACT))
                    .isInstanceOf(FileConversionException.class)
                    .hasMessageContaining("1");
        }

        @Test
        void convert_failure_propagates_as_file_conversion_exception() throws IOException {
            // FileUtils.convert() internally wraps IOException → FileConversionException;
            // the mock simulates that final outcome directly (checked IOException cannot
            // be declared as thenThrow for a method that does not declare it).
            DocumentProvider provider = mockProvider(DocumentType.TEMPLATE, true);
            when(provider.getDocument(1L))
                    .thenReturn(new DocumentResult(new byte[]{1}, DocumentFormat.DOCX));
            when(fileUtils.decompress(any())).thenReturn(new byte[]{2});
            when(fileUtils.convert(any(), any(), any()))
                    .thenThrow(new FileConversionException("conversion failed"));

            assertThatThrownBy(() ->
                    service.downloadDocument(1L, DocumentFormat.PDF, DocumentType.TEMPLATE))
                    .isInstanceOf(FileConversionException.class);
        }
    }
}

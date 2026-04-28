package clm.demo.utils;

import clm.demo.exceptions.FileConversionException;
import clm.demo.exceptions.UnsupportedConversionException;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.utils.file.FileUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * verifies GZIP compress / decompress round-trips and the same-format
 * short-circuit in convert(). LibreOffice-dependent conversion paths
 * are omitted from unit tests — they require an external process.
 */
class FileUtilsTest {

    // instantiated directly — FileUtils is a @Component but has no Spring wiring required for GZIP
    private final FileUtils fileUtils = new FileUtils("libreoffice");

    // ================================================================== //
    //  compress                                                            //
    // ================================================================== //

    @Nested
    class Compress {

        @Test
        void null_input_returns_empty_array() throws IOException {
            assertThat(fileUtils.compress(null)).isEmpty();
        }

        @Test
        void empty_input_returns_empty_array() throws IOException {
            assertThat(fileUtils.compress(new byte[0])).isEmpty();
        }

        @Test
        void output_is_non_empty_gzip_for_non_empty_input() throws IOException {
            byte[] result = fileUtils.compress(new byte[]{1, 2, 3, 4, 5});
            // gzip magic: 0x1F 0x8B
            assertThat(result).isNotEmpty();
            assertThat(result[0]).isEqualTo((byte) 0x1F);
            assertThat(result[1]).isEqualTo((byte) 0x8B);
        }

        @Test
        void repeated_compression_of_same_data_produces_same_length() throws IOException {
            byte[] data = "stable content".getBytes();
            assertThat(fileUtils.compress(data).length)
                    .isEqualTo(fileUtils.compress(data).length);
        }
    }

    // ================================================================== //
    //  decompress                                                          //
    // ================================================================== //

    @Nested
    class Decompress {

        @Test
        void null_input_returns_empty_array() throws IOException {
            assertThat(fileUtils.decompress(null)).isEmpty();
        }

        @Test
        void empty_input_returns_empty_array() throws IOException {
            assertThat(fileUtils.decompress(new byte[0])).isEmpty();
        }

        @Test
        void non_gzip_bytes_throw_io_exception() {
            byte[] notGzip = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};

            assertThatThrownBy(() -> fileUtils.decompress(notGzip))
                    .isInstanceOf(IOException.class);
        }
    }

    // ================================================================== //
    //  round-trip                                                          //
    // ================================================================== //

    @Nested
    class RoundTrip {

        @Test
        void compress_then_decompress_restores_original_bytes() throws IOException {
            byte[] original = "Hello, CLM platform!".getBytes();

            byte[] decompressed = fileUtils.decompress(fileUtils.compress(original));

            assertThat(decompressed).isEqualTo(original);
        }

        @Test
        void round_trip_works_for_full_byte_range() throws IOException {
            byte[] original = new byte[256];
            for (int i = 0; i < 256; i++) original[i] = (byte) i;

            byte[] decompressed = fileUtils.decompress(fileUtils.compress(original));

            assertThat(decompressed).isEqualTo(original);
        }

        @Test
        void compressed_form_is_smaller_than_original_for_repetitive_data() throws IOException {
            byte[] repetitive = new byte[2000];
            Arrays.fill(repetitive, (byte) 'A');

            byte[] compressed = fileUtils.compress(repetitive);

            assertThat(compressed.length).isLessThan(repetitive.length);
        }

        @Test
        void large_payload_round_trips_correctly() throws IOException {
            byte[] large = new byte[100_000];
            for (int i = 0; i < large.length; i++) large[i] = (byte) (i % 256);

            byte[] result = fileUtils.decompress(fileUtils.compress(large));

            assertThat(result).isEqualTo(large);
        }
    }

    // ================================================================== //
    //  convert — same-format short-circuit (no LibreOffice needed)        //
    // ================================================================== //

    @Nested
    class ConvertSameFormat {

        @Test
        void pdf_to_pdf_returns_exact_same_reference() {
            byte[] data = {1, 2, 3};

            byte[] result = fileUtils.convert(data, DocumentFormat.PDF, DocumentFormat.PDF);

            assertThat(result).isSameAs(data);
        }

        @Test
        void docx_to_docx_returns_exact_same_reference() {
            byte[] data = {4, 5, 6};

            byte[] result = fileUtils.convert(data, DocumentFormat.DOCX, DocumentFormat.DOCX);

            assertThat(result).isSameAs(data);
        }
    }
}

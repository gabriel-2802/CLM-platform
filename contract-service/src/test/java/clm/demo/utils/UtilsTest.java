package clm.demo.utils;

import clm.demo.exceptions.UnsupportedFileException;
import clm.demo.models.enums.DataType;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.support.TestDataFactory;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UtilsTest {

    // ================================================================== //
    //  detectDocumentFormat                                                //
    // ================================================================== //

    @Nested
    class DetectDocumentFormat {

        @Test
        void pdf_magic_bytes_detected_as_pdf() {
            assertThat(Utils.detectDocumentFormat(TestDataFactory.pdfMagicBytes()))
                    .isEqualTo(DocumentFormat.PDF);
        }

        @Test
        void docx_magic_bytes_detected_as_docx() {
            assertThat(Utils.detectDocumentFormat(TestDataFactory.docxMagicBytes()))
                    .isEqualTo(DocumentFormat.DOCX);
        }

        @Test
        void null_bytes_throws_unsupported_file_exception() {
            assertThatThrownBy(() -> Utils.detectDocumentFormat(null))
                    .isInstanceOf(UnsupportedFileException.class)
                    .hasMessageContaining("insufficient data");
        }

        @Test
        void empty_array_throws_unsupported_file_exception() {
            assertThatThrownBy(() -> Utils.detectDocumentFormat(new byte[0]))
                    .isInstanceOf(UnsupportedFileException.class);
        }

        @Test
        void three_bytes_too_short_throws() {
            // need at least 4 bytes
            assertThatThrownBy(() -> Utils.detectDocumentFormat(new byte[]{0x25, 0x50, 0x44}))
                    .isInstanceOf(UnsupportedFileException.class);
        }

        @Test
        void unknown_magic_bytes_throws_unsupported_file_exception() {
            assertThatThrownBy(() -> Utils.detectDocumentFormat(TestDataFactory.unknownFormatBytes()))
                    .isInstanceOf(UnsupportedFileException.class)
                    .hasMessageContaining("Supported formats: PDF, DOCX");
        }

        @Test
        void exactly_four_bytes_with_pdf_signature_detected() {
            byte[] exact = {0x25, 0x50, 0x44, 0x46};
            assertThat(Utils.detectDocumentFormat(exact)).isEqualTo(DocumentFormat.PDF);
        }

        @Test
        void exactly_four_bytes_with_docx_signature_detected() {
            byte[] exact = {0x50, 0x4B, 0x03, 0x04};
            assertThat(Utils.detectDocumentFormat(exact)).isEqualTo(DocumentFormat.DOCX);
        }
    }

    // ================================================================== //
    //  convertStringToDataType                                             //
    // ================================================================== //

    @Nested
    class ConvertStringToDataType {

        @ParameterizedTest(name = "null or blank → STRING")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   ", "\t"})
        void null_or_blank_returns_string(String input) {
            assertThat(Utils.convertStringToDataType(input)).isEqualTo(DataType.STRING);
        }

        @Test
        void string_literal_returns_string_enum() {
            assertThat(Utils.convertStringToDataType("STRING")).isEqualTo(DataType.STRING);
        }

        @Test
        void lowercase_string_still_works() {
            assertThat(Utils.convertStringToDataType("string")).isEqualTo(DataType.STRING);
        }

        @Test
        void mixed_case_still_works() {
            assertThat(Utils.convertStringToDataType("String")).isEqualTo(DataType.STRING);
        }

        @Test
        void date_type_recognized() {
            assertThat(Utils.convertStringToDataType("DATE")).isEqualTo(DataType.DATE);
        }

        @Test
        void number_type_recognized() {
            assertThat(Utils.convertStringToDataType("NUMBER")).isEqualTo(DataType.NUMBER);
        }

        @Test
        void boolean_type_recognized() {
            assertThat(Utils.convertStringToDataType("BOOLEAN")).isEqualTo(DataType.BOOLEAN);
        }

        @Test
        void currency_type_recognized() {
            assertThat(Utils.convertStringToDataType("CURRENCY")).isEqualTo(DataType.CURRENCY);
        }

        @Test
        void unknown_type_falls_back_to_string() {
            // no exception — graceful fallback with a log warning
            assertThat(Utils.convertStringToDataType("UNKNOWN_TYPE")).isEqualTo(DataType.STRING);
        }
    }

    // ================================================================== //
    //  getContentType                                                      //
    // ================================================================== //

    @Nested
    class GetContentType {

        @Test
        void pdf_returns_application_pdf() {
            assertThat(Utils.getContentType(DocumentFormat.PDF))
                    .isEqualTo("application/pdf");
        }

        @Test
        void docx_returns_correct_mime_type() {
            assertThat(Utils.getContentType(DocumentFormat.DOCX))
                    .isEqualTo("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        }
    }
}

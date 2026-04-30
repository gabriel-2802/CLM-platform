package clm.demo.utils;

import clm.demo.exceptions.exceptions.UnsupportedFileException;
import clm.demo.models.enums.DataType;
import clm.demo.models.enums.DocumentFormat;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
@UtilityClass
public class Utils {

    private static final byte[] PDF_MAGIC  = { 0x25, 0x50, 0x44, 0x46 }; // %PDF
    private static final byte[] DOCX_MAGIC = { 0x50, 0x4B };             // PK (ZIP)

    private static final String CONTENT_TYPE_PDF  = "application/pdf";
    private static final String CONTENT_TYPE_DOCX =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    /**
     * Checks the format of a file using magic bytes.
     *
     * @param fileBytes binary file content
     * @return the detected {@link DocumentFormat}
     * @throws UnsupportedFileException if the file is null, too short, or unrecognised
     */
    public DocumentFormat detectDocumentFormat(byte[] fileBytes) {
        if (Objects.isNull(fileBytes) || fileBytes.length < PDF_MAGIC.length) {
            throw new UnsupportedFileException("Invalid file: insufficient data to determine format");
        }
        if (startsWith(fileBytes, PDF_MAGIC)) {
            return DocumentFormat.PDF;
        }
        if (startsWith(fileBytes, DOCX_MAGIC)) {
            return DocumentFormat.DOCX;
        }
        throw new UnsupportedFileException(
                "File content does not match a supported format. Supported formats: PDF, DOCX");
    }

    /**
     * Converts a raw string to its {@link DataType} enum equivalent,
     * defaulting to {@link DataType#STRING} for null, blank, or unrecognised input.
     *
     * @param dataTypeStr the raw string representation of the data type
     * @return the matching {@link DataType}, or {@link DataType#STRING} as fallback
     */
    public DataType convertStringToDataType(String dataTypeStr) {
        if (Objects.isNull(dataTypeStr) || dataTypeStr.isBlank()) {
            return DataType.STRING;
        }
        try {
            return DataType.valueOf(dataTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown data type '{}', defaulting to STRING", dataTypeStr);
            return DataType.STRING;
        }
    }

    /**
     * Returns the MIME content-type string for a given {@link DocumentFormat}.
     *
     * @param format the document format
     * @return the corresponding MIME type string
     */
    public String getContentType(DocumentFormat format) {
        return switch (format) {
            case PDF  -> CONTENT_TYPE_PDF;
            case DOCX -> CONTENT_TYPE_DOCX;
        };
    }

    private boolean startsWith(byte[] data, byte[] magic) {
        if (data.length < magic.length) return false;
        for (int i = 0; i < magic.length; i++) {
            if (data[i] != magic[i]) return false;
        }
        return true;
    }
}
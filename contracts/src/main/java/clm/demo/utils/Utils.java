package clm.demo.utils;

import clm.demo.exceptions.UnsupportedFileException;
import clm.demo.models.enums.DataType;
import clm.demo.models.enums.DocumentFormat;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Utils {

    /**
     *  Checks the format of a file using magic BYTES
     * @param fileBytes binary file
     * @return the DOCUMENT FORMAT
     */
    public static DocumentFormat detectDocumentFormat(byte[] fileBytes) {
        if (fileBytes == null || fileBytes.length < 4) {
            throw new UnsupportedFileException("Invalid file: insufficient data to determine format");
        }
        if (fileBytes[0] == 0x25 && fileBytes[1] == 0x50 &&
                fileBytes[2] == 0x44 && fileBytes[3] == 0x46) {
            return DocumentFormat.PDF;
        }
        if (fileBytes[0] == 0x50 && fileBytes[1] == 0x4B) {
            return DocumentFormat.DOCX;
        }
        throw new UnsupportedFileException("File content does not match a supported format. Supported formats: PDF, DOCX");
    }

        /**
     * Helper method to convert string data type to enum.
     */
    public static DataType convertStringToDataType(String dataTypeStr) {
        if (dataTypeStr == null || dataTypeStr.trim().isEmpty()) {
            return DataType.STRING;
        }
        try {
            return DataType.valueOf(dataTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return DataType.STRING;
        }
    }

    public static String getContentType(DocumentFormat format) {
        return switch (format) {
            case PDF  -> "application/pdf";
            case DOCX -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            default   -> "application/octet-stream";
        };
    }
}

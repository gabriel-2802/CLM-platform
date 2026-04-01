package clm.demo.utils;

import lombok.experimental.UtilityClass;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;
import java.util.zip.GZIPInputStream;

/**
 * Utility class for binary data compression using the GZIP format.
 * * <p>Used to reduce the storage footprint of 'BYTEA' columns in the database,
 * particularly for large PDF and DOCX contract templates and generated outputs.</p>
 */
@UtilityClass
public class ZipUtils {

    private static final int BUFFER_SIZE = 65536; // 64KB

    /**
     * Compresses raw byte data into GZIP format.
     *
     * @param data the uncompressed source bytes (e.g., a raw PDF or DOCX file)
     * @return a byte array containing the GZIP-compressed data
     * @throws IOException if a streaming or compression error occurs
     */
    public static byte[] compress(byte[] data) throws IOException {
        if (data == null || data.length == 0) return new byte[0];

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             GZIPOutputStream gzip = new GZIPOutputStream(baos, BUFFER_SIZE)) {
            gzip.write(data);
            gzip.finish();
            return baos.toByteArray();
        }
    }

    /**
     * Decompresses GZIP-compressed data back to its original form.
     *
     * @param compressed the GZIP-encoded byte array retrieved from storage
     * @return the original uncompressed byte array
     * @throws IOException if the data is not valid GZIP format or is corrupted
     */
    public static byte[] decompress(byte[] compressed) throws IOException {
        if (compressed == null || compressed.length == 0) return new byte[0];

        try (GZIPInputStream gzip = new GZIPInputStream(
                new ByteArrayInputStream(compressed), BUFFER_SIZE)) {
            return gzip.readAllBytes();
        }
    }
}
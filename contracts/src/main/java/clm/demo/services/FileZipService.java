package clm.demo.services;

import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;
import java.util.zip.GZIPInputStream;

/**
 * Utility service for binary data compression using the GZIP format.
 * * <p>Used to reduce the storage footprint of 'BYTEA' columns in the database,
 * particularly for large PDF and DOCX contract templates and generated outputs.</p>
 */
@Service
public class FileZipService {

    /**
     * Compresses raw byte data into GZIP format.
     *
     * @param data The uncompressed source bytes (e.g., a raw PDF file).
     * @return A byte array containing the GZIP-compressed data.
     * @throws IOException If a streaming or compression error occurs.
     */
    public byte[] compress(byte[] data) throws IOException {
        if (data == null || data.length == 0) return data;

        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(boas)) {
            gzip.write(data);
        }
        return boas.toByteArray();
    }

    /**
     * Decompresses GZIP-compressed data back to its original state.
     *
     * @param compressed The GZIP-encoded byte array retrieved from storage.
     * @return The original uncompressed byte array.
     * @throws IOException If the data is not in valid GZIP format or is corrupted.
     */
    public byte[] decompress(byte[] compressed) throws IOException {
        if (compressed == null || compressed.length == 0) return compressed;

        ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
        try (GZIPInputStream gzip = new GZIPInputStream(bais)) {
            return gzip.readAllBytes();
        }
    }
}
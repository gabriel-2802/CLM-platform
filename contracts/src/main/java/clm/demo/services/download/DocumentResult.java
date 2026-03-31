package clm.demo.services.download;

import clm.demo.models.enums.DocumentFormat;

/**
 * Carries the compressed document bytes together with the native format
 * so that providers only need a single repository call per download request.
 *
 * @param compressedContent the raw, compressed document bytes
 * @param nativeFormat      the format in which the document is stored
 */
public record DocumentResult(byte[] compressedContent, DocumentFormat nativeFormat) {}
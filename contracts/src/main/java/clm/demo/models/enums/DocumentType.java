package clm.demo.models.enums;

/**
 * Identifies the kind of document requested from {@code DocumentDownloadService}.
 * Used by the provider registry — NOT a JPA discriminator.
 */
public enum DocumentType {
    TEMPLATE,
    UNSIGNED_CONTRACT,
    SIGNED_CONTRACT,
    UNSIGNED_APPENDIX,
    SIGNED_APPENDIX
}

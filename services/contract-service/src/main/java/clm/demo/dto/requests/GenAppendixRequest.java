package clm.demo.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * Request to generate a fillable appendix from a template and attach it to a contract.
 *
 * @param mappings label-to-value pairs used to fill template placeholders
 */
public record GenAppendixRequest(
        @NotNull(message = "Contract ID is required") Long contractId,
        @NotNull(message = "Template ID is required") Long templateId,
        @NotBlank(message = "Title is required") String title,
        Long userId,
        String notes,
        @NotEmpty(message = "At least one field mapping is required") Map<String, String> mappings
) {}

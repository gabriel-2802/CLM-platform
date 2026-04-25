package clm.demo.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * @param mappings LABEL - VALUE MAPPINGS
 * @param value    contract monetary value; {@code BigDecimal} used instead of
 *                 {@code Double} to avoid floating-point rounding issues in financial contexts
 */
public record GenContractRequest(@NotNull(message = "Template ID is required") Long templateId,
                                 @NotNull(message = "User ID is required") Long userId,
                                 @NotBlank(message = "User email is required") @Email(message = "Invalid email format") String userMail,
                                 @NotNull(message = "Client ID is required") Long clientId,
                                 @NotNull(message = "Start date is required") LocalDate startDate,
                                 @NotNull(message = "End date is required") LocalDate endDate,
                                 @NotNull(message = "Field mappings map is required") Map<String, String> mappings,
                                 @NotNull(message = "Auto-renewal flag is required") Boolean autoRenew,
                                 BigDecimal value, String notes) {

}
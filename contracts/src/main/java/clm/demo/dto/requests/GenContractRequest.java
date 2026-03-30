package clm.demo.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class GenContractRequest {

    @NotNull(message = "Template ID is required")
    private final Long templateId;

    @NotNull(message = "User ID is required")
    private final Long userId;

    @NotBlank(message = "User email is required")
    @Email(message = "Invalid email format")
    private final String userMail;

    @NotNull(message = "Client ID is required")
    private final Long clientId;

    @NotNull(message = "Start date is required")
    private final LocalDate startDate;

    @NotNull(message = "End date is required")
    private final LocalDate endDate;

    // LABEL - VALUE MAPPINGS
    @NotEmpty(message = "At least one field mapping is required")
    private final Map<String, String> mappings;

    private final Double value;

    private final String notes;
}
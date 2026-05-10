package clm.negotiation.dto.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateNegotiationRequest(

        @NotNull(message = "contractId is required")
        Long contractId,

        @NotNull(message = "clientId is required")
        Integer clientId,

        @Positive(message = "proposedValue must be positive")
        BigDecimal proposedValue,

        LocalDate proposedEndDate,

        @NotNull(message = "createdByUserId is required")
        Integer createdByUserId,

        @Size(max = 2000, message = "notes must not exceed 2000 characters")
        String notes
) {}

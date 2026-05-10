package clm.negotiation.dto.responses;

import clm.negotiation.models.enums.NegotiationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record NegotiationResponseDTO(
        Long id,
        Long contractId,
        Integer clientId,
        BigDecimal proposedValue,
        LocalDate proposedEndDate,
        NegotiationStatus status,
        String notes,
        Integer createdByUserId,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt
) {}

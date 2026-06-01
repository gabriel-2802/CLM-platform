package clm.negotiation.dto.requests;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ContractDeactivatedRequest(

        @NotEmpty(message = "contractIds must not be empty")
        List<Long> contractIds
) {}

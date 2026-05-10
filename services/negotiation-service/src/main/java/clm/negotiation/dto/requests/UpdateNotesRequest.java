package clm.negotiation.dto.requests;

import jakarta.validation.constraints.Size;

public record UpdateNotesRequest(
        @Size(max = 2000, message = "notes must not exceed 2000 characters")
        String notes
) {}

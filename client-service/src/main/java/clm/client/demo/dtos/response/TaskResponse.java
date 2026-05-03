package clm.client.demo.dtos.response;

import java.time.LocalDate;

public record TaskResponse(
        Long id,
        boolean done,
        String title,
        String notes,
        String blocked,
        String objective,
        LocalDate date,
        Long userId,
        Long clientId,
        String clientName,
        String clientType
) {}
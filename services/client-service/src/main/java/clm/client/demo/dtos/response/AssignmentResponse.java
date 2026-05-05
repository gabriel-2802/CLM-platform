package clm.client.demo.dtos.response;

import java.util.List;

public record AssignmentResponse(
    Long clientId,
    List<Long> userIds
) {
}


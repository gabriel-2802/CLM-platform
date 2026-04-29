package clm.client.demo.dtos.request;

import java.util.List;

public record AssignmentRequest(
    List<Long> userIds
) {
}


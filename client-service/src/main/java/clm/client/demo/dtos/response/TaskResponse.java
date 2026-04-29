package clm.client.demo.dtos.response;

import clm.client.demo.models.Task;

import java.time.LocalDateTime;

public record TaskResponse(
    Long id,
    boolean done,
    String title,
    String notes,
    String blocked,
    String objective,
    LocalDateTime date,
    Long userId,
    Long clientId,
    String clientName,
    String clientType
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.isDone(),
                task.getTitle(),
                task.getNotes(),
                task.getBlocked(),
                task.getObjective(),
                task.getDate(),
                task.getUserId(),
                task.getClient().getId(),
                task.getClient().getName(),
                task.getClient().getType() != null ? task.getClient().getType().name() : null
        );
    }
}

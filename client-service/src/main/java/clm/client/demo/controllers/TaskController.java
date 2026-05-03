package clm.client.demo.controllers;

import clm.client.demo.dtos.request.TaskRequest;
import clm.client.demo.dtos.response.TaskResponse;
import clm.client.demo.security.SecurityUtils;
import clm.client.demo.services.TaskService;
import clm.client.demo.validation.ValidationGroups;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task management endpoints.")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    @Operation(summary = "List tasks — USER sees only their own, MANAGER/ADMIN see all")
    public ResponseEntity<List<TaskResponse>> listTasks() {
        if (SecurityUtils.isUserOnly()) {
            Long userId = SecurityUtils.getCurrentUserId()
                    .orElseThrow(() -> new AccessDeniedException("Missing authenticated user id"));
            return ResponseEntity.ok(taskService.listTasksByUser(userId));
        }
        return ResponseEntity.ok(taskService.listAllTasks());
    }

    @GetMapping("/by-client/{clientId}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    @Operation(summary = "List tasks for a specific client")
    public ResponseEntity<List<TaskResponse>> listByClient(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long clientId) {
        return ResponseEntity.ok(taskService.listTasksByClient(clientId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Get task by ID")
    public ResponseEntity<TaskResponse> getTask(
            @Parameter(description = "Task ID", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTask(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Create a task")
    public ResponseEntity<TaskResponse> createTask(
            @Validated(ValidationGroups.Create.class) @RequestBody TaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(request));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Partially update a task")
    public ResponseEntity<TaskResponse> updateTask(
            @Parameter(description = "Task ID", required = true, example = "1")
            @PathVariable Long id,
            @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Delete a task")
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "Task ID", required = true, example = "1")
            @PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
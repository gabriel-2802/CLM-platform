package clm.client.demo.controllers;

import clm.client.demo.config.WebMvcTestSecurityConfig;
import clm.client.demo.dtos.request.TaskRequest;
import clm.client.demo.dtos.response.TaskResponse;
import clm.client.demo.exceptions.ResourceNotFoundException;
import clm.client.demo.services.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TaskController.class)
@Import(WebMvcTestSecurityConfig.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    private TaskResponse sampleTask() {
        return new TaskResponse(1L, false, "Test Task", "Some notes", null, "objective",
                LocalDate.of(2024, 6, 1), 1L, 1L, "ACME Corp", "SRL");
    }

    @Nested
    class ListTasks {

        @Test
        @WithMockUser(roles = "MANAGER", username = "1")
        void shouldReturnAllTasksForManager() throws Exception {
            // Given
            when(taskService.listAllTasks()).thenReturn(List.of(sampleTask()));

            // When / Then
            mockMvc.perform(get("/api/tasks"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].title").value("Test Task"));

            verify(taskService).listAllTasks();
            verify(taskService, never()).listTasksByUser(any());
        }

        @Test
        @WithMockUser(roles = "ADMIN", username = "2")
        void shouldReturnAllTasksForAdmin() throws Exception {
            // Given
            when(taskService.listAllTasks()).thenReturn(List.of(sampleTask()));

            // When / Then
            mockMvc.perform(get("/api/tasks"))
                    .andExpect(status().isOk());

            verify(taskService).listAllTasks();
        }

        @Test
        @WithMockUser(roles = "USER", username = "42")
        void shouldReturnOnlyOwnTasksForUser() throws Exception {
            // Given
            when(taskService.listTasksByUser(42L)).thenReturn(List.of(sampleTask()));

            // When / Then
            mockMvc.perform(get("/api/tasks"))
                    .andExpect(status().isOk());

            verify(taskService).listTasksByUser(42L);
            verify(taskService, never()).listAllTasks();
        }

        @Test
        void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/tasks"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class ListByClient {

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnTasksForClient() throws Exception {
            // Given
            when(taskService.listTasksByClient(1L)).thenReturn(List.of(sampleTask()));

            // When / Then
            mockMvc.perform(get("/api/tasks/by-client/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].clientId").value(1));
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnEmptyListWhenClientHasNoTasks() throws Exception {
            // Given
            when(taskService.listTasksByClient(99L)).thenReturn(List.of());

            // When / Then
            mockMvc.perform(get("/api/tasks/by-client/99"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/tasks/by-client/1"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class GetTask {

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnTaskWhenIdExists() throws Exception {
            // Given
            when(taskService.getTask(1L)).thenReturn(sampleTask());

            // When / Then
            mockMvc.perform(get("/api/tasks/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Test Task"))
                    .andExpect(jsonPath("$.done").value(false));
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnNotFoundWhenTaskDoesNotExist() throws Exception {
            // Given
            when(taskService.getTask(99L)).thenThrow(new ResourceNotFoundException("Task not found: 99"));

            // When / Then
            mockMvc.perform(get("/api/tasks/99"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/tasks/1"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class CreateTask {

        @Test
        @WithMockUser(roles = "USER")
        void shouldCreateTaskAndReturn201() throws Exception {
            // Given
            TaskRequest request = new TaskRequest(false, "New Task", null, null, null,
                    LocalDate.of(2024, 6, 1), 1L, 1L);
            when(taskService.createTask(any())).thenReturn(sampleTask());

            // When / Then
            mockMvc.perform(post("/api/tasks")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Test Task"));

            verify(taskService).createTask(any());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void shouldCreateTaskAsManager() throws Exception {
            // Given
            TaskRequest request = new TaskRequest(false, "Manager Task", null, null, null,
                    LocalDate.of(2024, 6, 1), 2L, 1L);
            when(taskService.createTask(any())).thenReturn(sampleTask());

            // When / Then
            mockMvc.perform(post("/api/tasks")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        @Test
        void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
            mockMvc.perform(post("/api/tasks")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class UpdateTask {

        @Test
        @WithMockUser(roles = "USER")
        void shouldPartiallyUpdateTask() throws Exception {
            // Given
            TaskRequest request = new TaskRequest(true, null, "Updated notes", null, null, null, null, null);
            when(taskService.updateTask(eq(1L), any())).thenReturn(sampleTask());

            // When / Then
            mockMvc.perform(patch("/api/tasks/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(taskService).updateTask(eq(1L), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnNotFoundWhenTaskDoesNotExistOnUpdate() throws Exception {
            // Given
            when(taskService.updateTask(eq(99L), any()))
                    .thenThrow(new ResourceNotFoundException("Task not found: 99"));

            // When / Then
            mockMvc.perform(patch("/api/tasks/99")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
            mockMvc.perform(patch("/api/tasks/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class DeleteTask {

        @Test
        @WithMockUser(roles = "MANAGER")
        void shouldDeleteTaskAndReturn204() throws Exception {
            // When / Then
            mockMvc.perform(delete("/api/tasks/1").with(csrf()))
                    .andExpect(status().isNoContent());

            verify(taskService).deleteTask(1L);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldDeleteTaskAsAdmin() throws Exception {
            // When / Then
            mockMvc.perform(delete("/api/tasks/1").with(csrf()))
                    .andExpect(status().isNoContent());

            verify(taskService).deleteTask(1L);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnForbiddenForUserRole() throws Exception {
            mockMvc.perform(delete("/api/tasks/1").with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void shouldReturnNotFoundWhenTaskDoesNotExistOnDelete() throws Exception {
            // Given
            doThrow(new ResourceNotFoundException("Task not found: 99"))
                    .when(taskService).deleteTask(99L);

            // When / Then
            mockMvc.perform(delete("/api/tasks/99").with(csrf()))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
            mockMvc.perform(delete("/api/tasks/1"))
                    .andExpect(status().isUnauthorized());
        }
    }
}

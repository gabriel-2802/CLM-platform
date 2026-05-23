package clm.client.demo.services;

import clm.client.demo.dtos.request.TaskRequest;
import clm.client.demo.dtos.response.TaskResponse;
import clm.client.demo.exceptions.ResourceNotFoundException;
import clm.client.demo.mappers.TaskMapper;
import clm.client.demo.models.Client;
import clm.client.demo.models.Task;
import clm.client.demo.repositories.ClientRepository;
import clm.client.demo.repositories.TaskRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    @Nested
    class ListTasksByUser {

        @Test
        void shouldReturnTasksForUser() {
            // Given
            Long userId = 1L;
            Task task = new Task();
            TaskResponse response = mock(TaskResponse.class);
            when(taskRepository.findAllByUserIdOrderByDateDesc(userId)).thenReturn(List.of(task));
            when(taskMapper.toResponse(task)).thenReturn(response);

            // When
            List<TaskResponse> result = taskService.listTasksByUser(userId);

            // Then
            assertThat(result).containsExactly(response);
            verify(taskRepository).findAllByUserIdOrderByDateDesc(userId);
        }

        @Test
        void shouldReturnEmptyListWhenNoTasksExistForUser() {
            // Given
            Long userId = 99L;
            when(taskRepository.findAllByUserIdOrderByDateDesc(userId)).thenReturn(List.of());

            // When
            List<TaskResponse> result = taskService.listTasksByUser(userId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class ListAllTasks {

        @Test
        void shouldReturnAllTasks() {
            // Given
            Task task = new Task();
            TaskResponse response = mock(TaskResponse.class);
            when(taskRepository.findAllWithClientOrderByDateDesc()).thenReturn(List.of(task));
            when(taskMapper.toResponse(task)).thenReturn(response);

            // When
            List<TaskResponse> result = taskService.listAllTasks();

            // Then
            assertThat(result).containsExactly(response);
            verify(taskRepository).findAllWithClientOrderByDateDesc();
        }

        @Test
        void shouldReturnEmptyListWhenNoTasksExist() {
            // Given
            when(taskRepository.findAllWithClientOrderByDateDesc()).thenReturn(List.of());

            // When
            List<TaskResponse> result = taskService.listAllTasks();

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class ListTasksByClient {

        @Test
        void shouldReturnTasksForClient() {
            // Given
            Long clientId = 1L;
            Task task = new Task();
            TaskResponse response = mock(TaskResponse.class);
            when(taskRepository.findAllByClientIdOrderByDateAsc(clientId)).thenReturn(List.of(task));
            when(taskMapper.toResponse(task)).thenReturn(response);

            // When
            List<TaskResponse> result = taskService.listTasksByClient(clientId);

            // Then
            assertThat(result).containsExactly(response);
            verify(taskRepository).findAllByClientIdOrderByDateAsc(clientId);
        }

        @Test
        void shouldReturnEmptyListWhenClientHasNoTasks() {
            // Given
            Long clientId = 99L;
            when(taskRepository.findAllByClientIdOrderByDateAsc(clientId)).thenReturn(List.of());

            // When
            List<TaskResponse> result = taskService.listTasksByClient(clientId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class GetTask {

        @Test
        void shouldReturnTaskWhenIdExists() {
            // Given
            Long id = 1L;
            Task task = new Task();
            TaskResponse response = mock(TaskResponse.class);
            when(taskRepository.findById(id)).thenReturn(Optional.of(task));
            when(taskMapper.toResponse(task)).thenReturn(response);

            // When
            TaskResponse result = taskService.getTask(id);

            // Then
            assertThat(result).isEqualTo(response);
        }

        @Test
        void shouldThrowResourceNotFoundExceptionWhenTaskDoesNotExist() {
            // Given
            Long id = 99L;
            when(taskRepository.findById(id)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> taskService.getTask(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Task not found: " + id);
        }
    }

    @Nested
    class CreateTask {

        @Test
        void shouldCreateAndReturnTask() {
            // Given
            TaskRequest request = new TaskRequest(false, "My Task", null, null, null,
                    LocalDate.of(2024, 6, 1), 1L, 1L);
            Client client = new Client();
            client.setId(1L);
            Task entity = new Task();
            Task saved = new Task();
            saved.setId(10L);
            saved.setTitle("My Task");
            TaskResponse response = mock(TaskResponse.class);

            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
            when(taskMapper.toEntity(request)).thenReturn(entity);
            when(taskRepository.save(entity)).thenReturn(saved);
            when(taskMapper.toResponse(saved)).thenReturn(response);

            // When
            TaskResponse result = taskService.createTask(request);

            // Then
            assertThat(result).isEqualTo(response);
            assertThat(entity.getClient()).isEqualTo(client);
            verify(taskRepository).save(entity);
        }

        @Test
        void shouldThrowResourceNotFoundExceptionWhenClientDoesNotExist() {
            // Given
            TaskRequest request = new TaskRequest(false, "Title", null, null, null,
                    LocalDate.of(2024, 6, 1), 1L, 99L);
            when(clientRepository.findById(99L)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> taskService.createTask(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Client not found: 99");
        }
    }

    @Nested
    class UpdateTask {

        @Test
        void shouldUpdateAndReturnTask() {
            // Given
            Long id = 1L;
            TaskRequest request = new TaskRequest(true, "Updated Title", null, null, null, null, 1L, null);
            Task existing = new Task();
            Task saved = new Task();
            saved.setId(id);
            TaskResponse response = mock(TaskResponse.class);

            when(taskRepository.findById(id)).thenReturn(Optional.of(existing));
            when(taskRepository.save(existing)).thenReturn(saved);
            when(taskMapper.toResponse(saved)).thenReturn(response);

            // When
            TaskResponse result = taskService.updateTask(id, request);

            // Then
            assertThat(result).isEqualTo(response);
            verify(taskMapper).partialUpdateEntity(existing, request);
            verify(taskRepository).save(existing);
        }

        @Test
        void shouldUpdateClientWhenClientIdIsProvided() {
            // Given
            Long id = 1L;
            Long newClientId = 5L;
            TaskRequest request = new TaskRequest(null, null, null, null, null, null, null, newClientId);
            Task existing = new Task();
            Client newClient = new Client();
            newClient.setId(newClientId);
            Task saved = new Task();
            TaskResponse response = mock(TaskResponse.class);

            when(taskRepository.findById(id)).thenReturn(Optional.of(existing));
            when(clientRepository.findById(newClientId)).thenReturn(Optional.of(newClient));
            when(taskRepository.save(existing)).thenReturn(saved);
            when(taskMapper.toResponse(saved)).thenReturn(response);

            // When
            taskService.updateTask(id, request);

            // Then
            assertThat(existing.getClient()).isEqualTo(newClient);
        }

        @Test
        void shouldThrowResourceNotFoundExceptionWhenTaskDoesNotExistOnUpdate() {
            // Given
            Long id = 99L;
            TaskRequest request = mock(TaskRequest.class);
            when(taskRepository.findById(id)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> taskService.updateTask(id, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Task not found: " + id);
        }

        @Test
        void shouldThrowResourceNotFoundExceptionWhenNewClientDoesNotExistOnUpdate() {
            // Given
            Long id = 1L;
            Long badClientId = 99L;
            TaskRequest request = new TaskRequest(null, null, null, null, null, null, null, badClientId);
            Task existing = new Task();

            when(taskRepository.findById(id)).thenReturn(Optional.of(existing));
            when(clientRepository.findById(badClientId)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> taskService.updateTask(id, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Client not found: " + badClientId);
        }
    }

    @Nested
    class DeleteTask {

        @Test
        void shouldDeleteTaskWhenExists() {
            // Given
            Long id = 1L;
            Task task = new Task();
            when(taskRepository.findById(id)).thenReturn(Optional.of(task));

            // When
            taskService.deleteTask(id);

            // Then
            verify(taskRepository).delete(task);
        }

        @Test
        void shouldThrowResourceNotFoundExceptionWhenTaskDoesNotExistOnDelete() {
            // Given
            Long id = 99L;
            when(taskRepository.findById(id)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> taskService.deleteTask(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Task not found: " + id);
        }
    }
}

package clm.client.demo.services;

import clm.client.demo.dtos.request.TaskRequest;
import clm.client.demo.dtos.response.TaskResponse;
import clm.client.demo.exceptions.ResourceNotFoundException;
import clm.client.demo.models.Client;
import clm.client.demo.models.Task;
import clm.client.demo.repositories.ClientRepository;
import clm.client.demo.repositories.TaskRepository;
import clm.client.demo.repositories.UserClientRepository;
import clm.client.demo.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final ClientRepository clientRepository;
    private final UserClientRepository userClientRepository;

    @Transactional(readOnly = true)
    public List<TaskResponse> listTasks() {
        if (SecurityUtils.isUserOnly()) {
            Long userId = SecurityUtils.getCurrentUserId()
                    .orElseThrow(() -> new AccessDeniedException("Missing authenticated user id"));
            return taskRepository.findAllByUserIdOrderByDateDesc(userId).stream()
                    .map(TaskResponse::from)
                    .toList();
        }
        return taskRepository.findAllWithClientOrderByDateDesc().stream()
                .map(TaskResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listTasksByClient(Long clientId) {
        return taskRepository.findAllByClientIdOrderByDateAsc(clientId).stream()
                .map(TaskResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(Long id) {
        Task task = findTask(id);
        enforceTaskAccess(task);
        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        Client client = clientRepository.findById(request.clientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + request.clientId()));

        Task task = new Task();
        task.setClient(client);
        task.setUserId(request.userId());
        task.setTitle(request.title());
        task.setDate(request.date());
        task.setDone(Boolean.TRUE.equals(request.done()));
        task.setNotes(request.notes());
        task.setBlocked(request.blocked());
        task.setObjective(request.objective());

        Task saved = taskRepository.save(task);
        log.info("Created task {} for client {}", saved.getId(), client.getId());
        return TaskResponse.from(saved);
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task task = findTask(id);

        if (Objects.nonNull(request.title())) task.setTitle(request.title());
        if (Objects.nonNull(request.date())) task.setDate(request.date());
        if (Objects.nonNull(request.done())) task.setDone(request.done());
        if (Objects.nonNull(request.notes())) task.setNotes(request.notes());
        if (Objects.nonNull(request.blocked())) task.setBlocked(request.blocked());
        if (Objects.nonNull(request.objective())) task.setObjective(request.objective());
        if (Objects.nonNull(request.userId())) task.setUserId(request.userId());

        if (Objects.nonNull(request.clientId())) {
            Client client = clientRepository.findById(request.clientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + request.clientId()));
            task.setClient(client);
        }

        Task saved = taskRepository.save(task);
        log.info("Updated task {}", saved.getId());
        return TaskResponse.from(saved);
    }

    @Transactional
    public void deleteTask(Long id) {
        Task task = findTask(id);
        taskRepository.delete(task);
        log.info("Deleted task {}", id);
    }

    private Task findTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
    }

    private void enforceTaskAccess(Task task) {
        if (!SecurityUtils.isUserOnly()) return;
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException("Missing authenticated user id"));
        if (!task.getUserId().equals(userId)) {
            throw new AccessDeniedException("Task access denied");
        }
    }
}

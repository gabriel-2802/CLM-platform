package clm.client.demo.services;

import clm.client.demo.dtos.request.TaskRequest;
import clm.client.demo.dtos.response.TaskResponse;
import clm.client.demo.exceptions.ResourceNotFoundException;
import clm.client.demo.mappers.TaskMapper;
import clm.client.demo.models.Client;
import clm.client.demo.models.Task;
import clm.client.demo.repositories.ClientRepository;
import clm.client.demo.repositories.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final ClientRepository clientRepository;
    private final TaskMapper taskMapper;

    @Transactional(readOnly = true)
    public List<TaskResponse> listTasksByUser(Long userId) {
        return taskRepository.findAllByUserIdOrderByDateDesc(userId).stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listAllTasks() {
        return taskRepository.findAllWithClientOrderByDateDesc().stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listTasksByClient(Long clientId) {
        return taskRepository.findAllByClientIdOrderByDateAsc(clientId).stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(Long id) {
        return taskMapper.toResponse(findTask(id));
    }

    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        Client client = findClient(request.clientId());
        Task task = taskMapper.toEntity(request);
        task.setClient(client);
        Task saved = taskRepository.save(task);
        log.info("created task {} for client {}", saved.getId(), client.getId());
        return taskMapper.toResponse(saved);
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task task = findTask(id);
        taskMapper.partialUpdateEntity(task, request);
        if (request.clientId() != null) {
            task.setClient(findClient(request.clientId()));
        }
        Task saved = taskRepository.save(task);
        log.info("updated task {}", saved.getId());
        return taskMapper.toResponse(saved);
    }

    @Transactional
    public void deleteTask(Long id) {
        taskRepository.delete(findTask(id));
        log.info("deleted task {}", id);
    }

    private Task findTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
    }

    private Client findClient(Long clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + clientId));
    }
}
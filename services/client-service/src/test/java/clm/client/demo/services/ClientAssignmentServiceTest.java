package clm.client.demo.services;

import clm.client.demo.dtos.request.AssignmentRequest;
import clm.client.demo.dtos.response.AssignmentResponse;
import clm.client.demo.exceptions.ResourceNotFoundException;
import clm.client.demo.models.Client;
import clm.client.demo.models.UserClient;
import clm.client.demo.repositories.ClientRepository;
import clm.client.demo.repositories.UserClientRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientAssignmentServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private UserClientRepository userClientRepository;

    @InjectMocks
    private ClientAssignmentService assignmentService;

    private Client sampleClient() {
        Client client = new Client();
        client.setId(1L);
        return client;
    }

    private UserClient sampleUserClient(Long clientId, Long userId) {
        UserClient uc = new UserClient();
        Client c = new Client();
        c.setId(clientId);
        uc.setClient(c);
        uc.setUserId(userId);
        return uc;
    }

    @Nested
    class GetAssignedUsers {

        @Test
        void shouldReturnAssignedUsers() {
            when(clientRepository.existsById(1L)).thenReturn(true);
            when(userClientRepository.findAllByClientId(1L)).thenReturn(List.of(
                    sampleUserClient(1L, 100L),
                    sampleUserClient(1L, 101L)
            ));

            AssignmentResponse result = assignmentService.getAssignedUsers(1L);

            assertThat(result).isNotNull();
            assertThat(result.clientId()).isEqualTo(1L);
            assertThat(result.userIds()).containsExactly(100L, 101L);
        }

        @Test
        void shouldThrowExceptionWhenClientNotFound() {
            when(clientRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> assignmentService.getAssignedUsers(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class ReplaceAssignments {

        @Test
        void shouldReplaceAssignments() {
            AssignmentRequest request = new AssignmentRequest(List.of(100L, 101L, 101L)); // duplicate 101L should be ignored
            when(clientRepository.findById(1L)).thenReturn(Optional.of(sampleClient()));

            AssignmentResponse result = assignmentService.replaceAssignments(1L, request);

            assertThat(result).isNotNull();
            assertThat(result.userIds()).hasSize(2).contains(100L, 101L);
            
            verify(userClientRepository).deleteByClientId(1L);
            verify(userClientRepository).saveAll(any());
        }

        @Test
        void shouldThrowExceptionWhenClientNotFound() {
            AssignmentRequest request = new AssignmentRequest(List.of(100L));
            when(clientRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> assignmentService.replaceAssignments(99L, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class AssignUser {

        @Test
        void shouldAssignUser() {
            when(clientRepository.findById(1L)).thenReturn(Optional.of(sampleClient()));

            assignmentService.assignUser(1L, 100L);

            verify(userClientRepository).save(any(UserClient.class));
        }

        @Test
        void shouldIgnoreDuplicateAssignment() {
            when(clientRepository.findById(1L)).thenReturn(Optional.of(sampleClient()));
            doThrow(DataIntegrityViolationException.class).when(userClientRepository).save(any(UserClient.class));

            assignmentService.assignUser(1L, 100L); // Should not throw

            verify(userClientRepository).save(any(UserClient.class));
        }

        @Test
        void shouldThrowExceptionWhenClientNotFound() {
            when(clientRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> assignmentService.assignUser(99L, 100L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class RemoveUser {

        @Test
        void shouldRemoveUser() {
            when(clientRepository.existsById(1L)).thenReturn(true);

            assignmentService.removeUser(1L, 100L);

            verify(userClientRepository).deleteByClientIdAndUserId(1L, 100L);
        }

        @Test
        void shouldThrowExceptionWhenClientNotFound() {
            when(clientRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> assignmentService.removeUser(99L, 100L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class GetClientsForUser {

        @Test
        void shouldReturnClientsForUser() {
            when(userClientRepository.findAllByUserId(100L)).thenReturn(List.of(
                    sampleUserClient(1L, 100L),
                    sampleUserClient(2L, 100L)
            ));

            List<Long> result = assignmentService.getClientsForUser(100L);

            assertThat(result).containsExactly(1L, 2L);
        }
    }
}

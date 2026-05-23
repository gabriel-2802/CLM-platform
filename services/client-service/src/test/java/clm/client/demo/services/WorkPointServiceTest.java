package clm.client.demo.services;

import clm.client.demo.dtos.request.WorkPointRequest;
import clm.client.demo.dtos.response.WorkPointResponse;
import clm.client.demo.exceptions.ResourceNotFoundException;
import clm.client.demo.mappers.WorkPointMapper;
import clm.client.demo.models.Client;
import clm.client.demo.models.WorkPoint;
import clm.client.demo.repositories.ClientRepository;
import clm.client.demo.repositories.WorkPointRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkPointServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private WorkPointRepository workPointRepository;

    @Mock
    private WorkPointMapper workPointMapper;

    @InjectMocks
    private WorkPointService workPointService;

    @Nested
    class ListWorkPoints {

        @Test
        void shouldReturnWorkPointsForClient() {
            // Given
            Long clientId = 1L;
            WorkPoint wp = new WorkPoint();
            WorkPointResponse response = mock(WorkPointResponse.class);
            when(workPointRepository.findAllByClientId(clientId)).thenReturn(List.of(wp));
            when(workPointMapper.toResponse(wp)).thenReturn(response);

            // When
            List<WorkPointResponse> result = workPointService.listWorkPoints(clientId);

            // Then
            assertThat(result).containsExactly(response);
            verify(workPointRepository).findAllByClientId(clientId);
        }

        @Test
        void shouldReturnEmptyListWhenClientHasNoWorkPoints() {
            // Given
            Long clientId = 99L;
            when(workPointRepository.findAllByClientId(clientId)).thenReturn(List.of());

            // When
            List<WorkPointResponse> result = workPointService.listWorkPoints(clientId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class GetWorkPoint {

        @Test
        void shouldReturnWorkPointWhenExists() {
            // Given
            Long clientId = 1L;
            Long id = 10L;
            WorkPoint wp = new WorkPoint();
            WorkPointResponse response = mock(WorkPointResponse.class);
            when(workPointRepository.findByIdAndClientId(id, clientId)).thenReturn(Optional.of(wp));
            when(workPointMapper.toResponse(wp)).thenReturn(response);

            // When
            WorkPointResponse result = workPointService.getWorkPoint(clientId, id);

            // Then
            assertThat(result).isEqualTo(response);
        }

        @Test
        void shouldThrowResourceNotFoundExceptionWhenWorkPointDoesNotExist() {
            // Given
            Long clientId = 1L;
            Long id = 99L;
            when(workPointRepository.findByIdAndClientId(id, clientId)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> workPointService.getWorkPoint(clientId, id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Work point not found: " + id);
        }

        @Test
        void shouldThrowResourceNotFoundExceptionWhenWorkPointBelongsToDifferentClient() {
            // Given
            Long clientId = 2L;
            Long id = 10L;
            when(workPointRepository.findByIdAndClientId(id, clientId)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> workPointService.getWorkPoint(clientId, id))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class CreateWorkPoint {

        @Test
        void shouldCreateAndReturnWorkPoint() {
            // Given
            Long clientId = 1L;
            WorkPointRequest request = mock(WorkPointRequest.class);
            Client client = new Client();
            client.setId(clientId);
            WorkPoint entity = new WorkPoint();
            WorkPoint saved = new WorkPoint();
            saved.setId(10L);
            WorkPointResponse response = mock(WorkPointResponse.class);

            when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
            when(workPointMapper.toEntity(request)).thenReturn(entity);
            when(workPointRepository.save(entity)).thenReturn(saved);
            when(workPointMapper.toResponse(saved)).thenReturn(response);

            // When
            WorkPointResponse result = workPointService.createWorkPoint(clientId, request);

            // Then
            assertThat(result).isEqualTo(response);
            assertThat(entity.getClient()).isEqualTo(client);
            verify(workPointRepository).save(entity);
        }

        @Test
        void shouldThrowResourceNotFoundExceptionWhenClientDoesNotExist() {
            // Given
            Long clientId = 99L;
            WorkPointRequest request = mock(WorkPointRequest.class);
            when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> workPointService.createWorkPoint(clientId, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Client not found: " + clientId);
        }
    }

    @Nested
    class UpdateWorkPoint {

        @Test
        void shouldUpdateAndReturnWorkPoint() {
            // Given
            Long clientId = 1L;
            Long id = 10L;
            WorkPointRequest request = mock(WorkPointRequest.class);
            WorkPoint existing = new WorkPoint();
            WorkPoint saved = new WorkPoint();
            WorkPointResponse response = mock(WorkPointResponse.class);

            when(workPointRepository.findByIdAndClientId(id, clientId)).thenReturn(Optional.of(existing));
            when(workPointRepository.save(existing)).thenReturn(saved);
            when(workPointMapper.toResponse(saved)).thenReturn(response);

            // When
            WorkPointResponse result = workPointService.updateWorkPoint(clientId, id, request);

            // Then
            assertThat(result).isEqualTo(response);
            verify(workPointMapper).updateEntity(existing, request);
            verify(workPointRepository).save(existing);
        }

        @Test
        void shouldThrowResourceNotFoundExceptionWhenWorkPointDoesNotExistOnUpdate() {
            // Given
            Long clientId = 1L;
            Long id = 99L;
            WorkPointRequest request = mock(WorkPointRequest.class);
            when(workPointRepository.findByIdAndClientId(id, clientId)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> workPointService.updateWorkPoint(clientId, id, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Work point not found: " + id);
        }
    }

    @Nested
    class DeleteWorkPoint {

        @Test
        void shouldDeleteWorkPointWhenExists() {
            // Given
            Long clientId = 1L;
            Long id = 10L;
            WorkPoint wp = new WorkPoint();
            when(workPointRepository.findByIdAndClientId(id, clientId)).thenReturn(Optional.of(wp));

            // When
            workPointService.deleteWorkPoint(clientId, id);

            // Then
            verify(workPointRepository).delete(wp);
        }

        @Test
        void shouldThrowResourceNotFoundExceptionWhenWorkPointDoesNotExistOnDelete() {
            // Given
            Long clientId = 1L;
            Long id = 99L;
            when(workPointRepository.findByIdAndClientId(id, clientId)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> workPointService.deleteWorkPoint(clientId, id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Work point not found: " + id);
        }
    }
}

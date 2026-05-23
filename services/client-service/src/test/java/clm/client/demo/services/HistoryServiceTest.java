package clm.client.demo.services;

import clm.client.demo.dtos.request.HistoryRequest;
import clm.client.demo.dtos.response.HistoryResponse;
import clm.client.demo.exceptions.ResourceNotFoundException;
import clm.client.demo.mappers.HistoryMapper;
import clm.client.demo.models.Client;
import clm.client.demo.models.ClientHistory;
import clm.client.demo.repositories.ClientHistoryRepository;
import clm.client.demo.repositories.ClientRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistoryServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientHistoryRepository historyRepository;

    @Mock
    private HistoryMapper historyMapper;

    @InjectMocks
    private HistoryService historyService;

    private Client sampleClient() {
        Client client = new Client();
        client.setId(1L);
        return client;
    }

    private ClientHistory sampleHistory() {
        ClientHistory history = new ClientHistory();
        history.setId(10L);
        history.setYear(2024);
        history.setClient(sampleClient());
        return history;
    }

    private HistoryResponse sampleResponse() {
        return new HistoryResponse(10L, 1L, 2024, null, null, null, null);
    }

    @Nested
    class ListHistory {
        @Test
        void shouldReturnHistories() {
            // Given
            when(historyRepository.findAllByClientId(1L)).thenReturn(List.of(sampleHistory()));
            when(historyMapper.toResponse(any(ClientHistory.class))).thenReturn(sampleResponse());

            // When
            List<HistoryResponse> result = historyService.listHistory(1L);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().year()).isEqualTo(2024);
        }
    }

    @Nested
    class GetHistory {

        @Test
        void shouldReturnHistoryWhenExists() {
            // Given
            ClientHistory history = sampleHistory();
            when(historyRepository.findByClientIdAndYear(1L, 2024)).thenReturn(Optional.of(history));
            when(historyMapper.toResponse(history)).thenReturn(sampleResponse());

            // When
            HistoryResponse result = historyService.getHistory(1L, 2024);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.year()).isEqualTo(2024);
        }

        @Test
        void shouldThrowExceptionWhenNotFound() {
            when(historyRepository.findByClientIdAndYear(1L, 2024)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> historyService.getHistory(1L, 2024))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("History not found for year: 2024");
        }
    }

    @Nested
    class UpsertHistory {

        @Test
        void shouldCreateHistoryWhenNotExists() {
            // Given
            HistoryRequest request = new HistoryRequest(2024, null, null, null, null);
            Client client = sampleClient();
            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
            when(historyRepository.findByClientIdAndYear(1L, 2024)).thenReturn(Optional.empty());
            when(historyRepository.save(any(ClientHistory.class))).thenAnswer(i -> i.getArgument(0));
            when(historyMapper.toResponse(any(ClientHistory.class))).thenReturn(sampleResponse());

            // When
            HistoryResponse result = historyService.upsertHistory(1L, 2024, request);

            // Then
            assertThat(result).isNotNull();
            verify(historyMapper).updateEntity(any(ClientHistory.class), eq(request));
            verify(historyRepository).save(any(ClientHistory.class));
        }

        @Test
        void shouldUpdateHistoryWhenExists() {
            // Given
            HistoryRequest request = new HistoryRequest(2024, null, null, null, null);
            Client client = sampleClient();
            ClientHistory history = sampleHistory();
            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
            when(historyRepository.findByClientIdAndYear(1L, 2024)).thenReturn(Optional.of(history));
            when(historyRepository.save(history)).thenReturn(history);
            when(historyMapper.toResponse(history)).thenReturn(sampleResponse());

            // When
            HistoryResponse result = historyService.upsertHistory(1L, 2024, request);

            // Then
            assertThat(result).isNotNull();
            verify(historyMapper).updateEntity(history, request);
            verify(historyRepository).save(history);
        }

        @Test
        void shouldThrowExceptionWhenYearMismatch() {
            HistoryRequest request = new HistoryRequest(2025, null, null, null, null);
            when(clientRepository.findById(1L)).thenReturn(Optional.of(sampleClient()));

            assertThatThrownBy(() -> historyService.upsertHistory(1L, 2024, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Request year does not match path year");
        }

        @Test
        void shouldThrowExceptionWhenClientNotFound() {
            HistoryRequest request = new HistoryRequest(2024, null, null, null, null);
            when(clientRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> historyService.upsertHistory(99L, 2024, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Client not found: 99");
        }
    }

    @Nested
    class DeleteHistory {

        @Test
        void shouldDeleteWhenExists() {
            ClientHistory history = sampleHistory();
            when(historyRepository.findByClientIdAndYear(1L, 2024)).thenReturn(Optional.of(history));

            historyService.deleteHistory(1L, 2024);

            verify(historyRepository).delete(history);
        }

        @Test
        void shouldThrowExceptionWhenNotFound() {
            when(historyRepository.findByClientIdAndYear(1L, 2024)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> historyService.deleteHistory(1L, 2024))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}

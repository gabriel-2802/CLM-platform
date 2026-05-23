package clm.client.demo.services;

import clm.client.demo.dtos.request.DetailsRequest;
import clm.client.demo.dtos.response.DetailsResponse;
import clm.client.demo.exceptions.ResourceNotFoundException;
import clm.client.demo.mappers.DetailsMapper;
import clm.client.demo.models.Client;
import clm.client.demo.models.ClientDetails;
import clm.client.demo.repositories.ClientDetailsRepository;
import clm.client.demo.repositories.ClientRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DetailsServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientDetailsRepository detailsRepository;

    @Mock
    private DetailsMapper detailsMapper;

    @InjectMocks
    private DetailsService detailsService;

    private Client sampleClient() {
        Client client = new Client();
        client.setId(1L);
        return client;
    }

    private ClientDetails sampleDetails() {
        ClientDetails details = new ClientDetails();
        details.setId(10L);
        details.setClient(sampleClient());
        return details;
    }

    private DetailsResponse sampleResponse() {
        return new DetailsResponse(10L, 1L, null, null, null, null, null, null, null, null, null);
    }

    @Nested
    class GetDetails {

        @Test
        void shouldReturnDetailsWhenExists() {
            // Given
            ClientDetails details = sampleDetails();
            DetailsResponse response = sampleResponse();
            when(detailsRepository.findByClientId(1L)).thenReturn(Optional.of(details));
            when(detailsMapper.toResponse(details)).thenReturn(response);

            // When
            DetailsResponse result = detailsService.getDetails(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(10L);
            verify(detailsRepository).findByClientId(1L);
        }

        @Test
        void shouldThrowExceptionWhenDetailsNotFound() {
            // Given
            when(detailsRepository.findByClientId(99L)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> detailsService.getDetails(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Details not found for client: 99");
        }
    }

    @Nested
    class UpsertDetails {

        @Test
        void shouldCreateDetailsWhenNotExists() {
            // Given
            DetailsRequest request = new DetailsRequest(null, null, null, null, null, null, null, null, null);
            Client client = sampleClient();
            DetailsResponse response = sampleResponse();
            
            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
            when(detailsRepository.findByClientId(1L)).thenReturn(Optional.empty());
            when(detailsRepository.save(any(ClientDetails.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(detailsMapper.toResponse(any(ClientDetails.class))).thenReturn(response);

            // When
            DetailsResponse result = detailsService.upsertDetails(1L, request);

            // Then
            assertThat(result).isNotNull();
            verify(detailsMapper).updateEntity(any(ClientDetails.class), eq(request));
            verify(detailsRepository).save(any(ClientDetails.class));
        }

        @Test
        void shouldUpdateDetailsWhenExists() {
            // Given
            DetailsRequest request = new DetailsRequest(null, null, null, null, null, null, null, null, null);
            Client client = sampleClient();
            ClientDetails details = sampleDetails();
            DetailsResponse response = sampleResponse();

            when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
            when(detailsRepository.findByClientId(1L)).thenReturn(Optional.of(details));
            when(detailsRepository.save(any(ClientDetails.class))).thenReturn(details);
            when(detailsMapper.toResponse(details)).thenReturn(response);

            // When
            DetailsResponse result = detailsService.upsertDetails(1L, request);

            // Then
            assertThat(result).isNotNull();
            verify(detailsMapper).updateEntity(details, request);
            verify(detailsRepository).save(details);
        }

        @Test
        void shouldThrowExceptionWhenClientNotFound() {
            // Given
            DetailsRequest request = new DetailsRequest(null, null, null, null, null, null, null, null, null);
            when(clientRepository.findById(99L)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> detailsService.upsertDetails(99L, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Client not found: 99");
            
            verify(detailsRepository, never()).save(any());
        }
    }

    @Nested
    class PatchDetails {

        @Test
        void shouldPatchDetailsWhenExists() {
            // Given
            DetailsRequest request = new DetailsRequest(null, null, null, null, null, null, null, null, null);
            ClientDetails details = sampleDetails();
            DetailsResponse response = sampleResponse();

            when(detailsRepository.findByClientId(1L)).thenReturn(Optional.of(details));
            when(detailsRepository.save(details)).thenReturn(details);
            when(detailsMapper.toResponse(details)).thenReturn(response);

            // When
            DetailsResponse result = detailsService.patchDetails(1L, request);

            // Then
            assertThat(result).isNotNull();
            verify(detailsMapper).partialUpdateEntity(details, request);
            verify(detailsRepository).save(details);
        }

        @Test
        void shouldThrowExceptionWhenDetailsNotFound() {
            // Given
            DetailsRequest request = new DetailsRequest(null, null, null, null, null, null, null, null, null);
            when(detailsRepository.findByClientId(99L)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> detailsService.patchDetails(99L, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Details not found for client: 99");
            
            verify(detailsRepository, never()).save(any());
        }
    }
}

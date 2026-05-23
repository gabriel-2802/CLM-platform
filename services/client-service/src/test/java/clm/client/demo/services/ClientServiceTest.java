package clm.client.demo.services;

import clm.client.demo.dtos.request.ClientListRequest;
import clm.client.demo.dtos.request.ClientRequest;
import clm.client.demo.dtos.response.ClientResponse;
import clm.client.demo.exceptions.ResourceNotFoundException;
import clm.client.demo.mappers.ClientMapper;
import clm.client.demo.models.Client;
import clm.client.demo.repositories.ClientRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @InjectMocks
    private ClientService clientService;

    @Nested
    class ListTemplateFields {
        @Test
        void shouldReturnTemplateFields() {
            List<String> fields = clientService.listTemplateFields();
            assertThat(fields).isNotNull();
        }
    }

    @Nested
    class ListClients {
        @Test
        void shouldReturnClientsPage() {
            ClientListRequest request = mock(ClientListRequest.class);
            when(request.page()).thenReturn(0);
            when(request.size()).thenReturn(10);
            
            Client client = new Client();
            Page<Client> clientPage = new PageImpl<>(List.of(client));
            
            when(clientRepository.findAll(any(Specification.class), any(PageRequest.class)))
                    .thenReturn(clientPage);
            
            ClientResponse response = mock(ClientResponse.class);
            when(clientMapper.toResponse(client)).thenReturn(response);

            Page<ClientResponse> result = clientService.listClients(request, 1L);

            assertThat(result.getContent()).containsExactly(response);
            verify(clientRepository).findAll(any(Specification.class), any(PageRequest.class));
        }
        
        @Test
        void shouldHandleNullPageAndSize() {
            ClientListRequest request = mock(ClientListRequest.class);
            when(request.page()).thenReturn(null);
            when(request.size()).thenReturn(null);
            
            Page<Client> clientPage = new PageImpl<>(List.of());
            when(clientRepository.findAll(any(Specification.class), any(PageRequest.class)))
                    .thenReturn(clientPage);

            clientService.listClients(request, null);

            verify(clientRepository).findAll(any(Specification.class), eq(PageRequest.of(0, 50)));
        }
    }

    @Nested
    class GetClientById {
        @Test
        void shouldReturnClientWhenIdExists() {
            Long id = 1L;
            Client client = new Client();
            when(clientRepository.findById(id)).thenReturn(Optional.of(client));
            
            ClientResponse response = mock(ClientResponse.class);
            when(clientMapper.toResponse(client)).thenReturn(response);

            ClientResponse result = clientService.getClientById(id);

            assertThat(result).isEqualTo(response);
        }

        @Test
        void shouldThrowExceptionWhenIdDoesNotExist() {
            Long id = 1L;
            when(clientRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clientService.getClientById(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Client not found: " + id);
        }
    }

    @Nested
    class CreateClient {
        @Test
        void shouldCreateAndReturnClient() {
            ClientRequest request = mock(ClientRequest.class);
            Client entity = new Client();
            Client savedEntity = new Client();
            savedEntity.setId(1L);
            savedEntity.setName("Test");
            
            when(clientMapper.toEntity(request)).thenReturn(entity);
            when(clientRepository.save(entity)).thenReturn(savedEntity);
            
            ClientResponse response = mock(ClientResponse.class);
            when(clientMapper.toResponse(savedEntity)).thenReturn(response);

            ClientResponse result = clientService.createClient(request);

            assertThat(result).isEqualTo(response);
            verify(clientRepository).save(entity);
        }
    }

    @Nested
    class UpdateClient {
        @Test
        void shouldUpdateAndReturnClient() {
            Long id = 1L;
            ClientRequest request = mock(ClientRequest.class);
            Client existingClient = new Client();
            
            when(clientRepository.findById(id)).thenReturn(Optional.of(existingClient));
            when(clientRepository.save(existingClient)).thenReturn(existingClient);
            
            ClientResponse response = mock(ClientResponse.class);
            when(clientMapper.toResponse(existingClient)).thenReturn(response);

            ClientResponse result = clientService.updateClient(id, request);

            assertThat(result).isEqualTo(response);
            verify(clientMapper).updateEntity(existingClient, request);
            verify(clientRepository).save(existingClient);
        }

        @Test
        void shouldThrowExceptionWhenClientNotFound() {
            Long id = 1L;
            ClientRequest request = mock(ClientRequest.class);
            when(clientRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clientService.updateClient(id, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class PartialUpdateClient {
        @Test
        void shouldPartiallyUpdateAndReturnClient() {
            Long id = 1L;
            ClientRequest request = mock(ClientRequest.class);
            Client existingClient = new Client();
            
            when(clientRepository.findById(id)).thenReturn(Optional.of(existingClient));
            when(clientRepository.save(existingClient)).thenReturn(existingClient);
            
            ClientResponse response = mock(ClientResponse.class);
            when(clientMapper.toResponse(existingClient)).thenReturn(response);

            ClientResponse result = clientService.partialUpdateClient(id, request);

            assertThat(result).isEqualTo(response);
            verify(clientMapper).partialUpdateEntity(existingClient, request);
            verify(clientRepository).save(existingClient);
        }
        
        @Test
        void shouldThrowExceptionWhenClientNotFound() {
            Long id = 1L;
            ClientRequest request = mock(ClientRequest.class);
            when(clientRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clientService.partialUpdateClient(id, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class DeleteClient {
        @Test
        void shouldDeleteClientWhenExists() {
            Long id = 1L;
            Client existingClient = new Client();
            when(clientRepository.findById(id)).thenReturn(Optional.of(existingClient));

            clientService.deleteClient(id);

            verify(clientRepository).delete(existingClient);
        }
        
        @Test
        void shouldThrowExceptionWhenClientNotFound() {
            Long id = 1L;
            when(clientRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clientService.deleteClient(id))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}

package clm.client.demo.controllers;

import clm.client.demo.config.WebMvcTestSecurityConfig;
import clm.client.demo.dtos.request.ClientListRequest;
import clm.client.demo.dtos.request.ClientRequest;
import clm.client.demo.dtos.response.ClientResponse;
import clm.client.demo.exceptions.ResourceNotFoundException;
import clm.client.demo.models.enums.Administration;
import clm.client.demo.models.enums.CompanyType;
import clm.client.demo.models.enums.TaxFrequency;
import clm.client.demo.services.ClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ClientController.class)
@Import(WebMvcTestSecurityConfig.class)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClientService clientService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Nested
    class ListTemplateFields {

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnTemplateFields() throws Exception {
            when(clientService.listTemplateFields()).thenReturn(List.of("id", "name"));

            mockMvc.perform(get("/api/clients/template-fields"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0]").value("id"))
                    .andExpect(jsonPath("$[1]").value("name"));
        }

        @Test
        void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/clients/template-fields"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class ListClients {

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnClientsPage() throws Exception {
            Page<ClientResponse> page = new PageImpl<>(List.of());
            when(clientService.listClients(any(), any())).thenReturn(page);

            mockMvc.perform(get("/api/clients"))
                    .andExpect(status().isOk());
        }

        @Test
        void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/clients"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class GetClientById {

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnClientWhenIdExists() throws Exception {
            Long id = 1L;
            when(clientService.getClientById(id)).thenReturn(null);

            mockMvc.perform(get("/api/clients/{id}", id))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnNotFoundWhenClientDoesNotExist() throws Exception {
            Long id = 1L;
            when(clientService.getClientById(id)).thenThrow(new ResourceNotFoundException("Not found"));

            mockMvc.perform(get("/api/clients/{id}", id))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/clients/1"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class CreateClient {

        private ClientRequest validRequest() {
            return new ClientRequest("ACME SRL", CompanyType.SRL, "RO12345678", true,
                    null, null, Administration.AJFP_CLUJ, null, TaxFrequency.DA_LUNAR,
                    null, null, null, null, null, null, null, null, null, null, null, null);
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void shouldCreateClient() throws Exception {
            mockMvc.perform(post("/api/clients")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnForbiddenForUserRole() throws Exception {
            mockMvc.perform(post("/api/clients")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
            mockMvc.perform(post("/api/clients")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class UpdateClient {

        private ClientRequest validRequest() {
            return new ClientRequest("ACME SRL", CompanyType.SRL, "RO12345678", true,
                    null, null, Administration.AJFP_CLUJ, null, TaxFrequency.DA_LUNAR,
                    null, null, null, null, null, null, null, null, null, null, null, null);
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void shouldUpdateClient() throws Exception {
            mockMvc.perform(put("/api/clients/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnForbiddenForUserRole() throws Exception {
            mockMvc.perform(put("/api/clients/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class PartialUpdateClient {

        @Test
        @WithMockUser(roles = "MANAGER")
        void shouldPartiallyUpdateClient() throws Exception {
            mockMvc.perform(patch("/api/clients/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnForbiddenForUserRole() throws Exception {
            mockMvc.perform(patch("/api/clients/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class DeleteClient {

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldDeleteClient() throws Exception {
            Long id = 1L;

            mockMvc.perform(delete("/api/clients/{id}", id).with(csrf()))
                    .andExpect(status().isNoContent());

            verify(clientService).deleteClient(id);
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void shouldReturnForbiddenForManagerRole() throws Exception {
            mockMvc.perform(delete("/api/clients/1").with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
            mockMvc.perform(delete("/api/clients/1"))
                    .andExpect(status().isUnauthorized());
        }
    }
}

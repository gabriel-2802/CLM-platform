package clm.client.demo.controllers;

import clm.client.demo.config.WebMvcTestSecurityConfig;
import clm.client.demo.dtos.request.AssignmentRequest;
import clm.client.demo.dtos.response.AssignmentResponse;
import clm.client.demo.exceptions.ResourceNotFoundException;
import clm.client.demo.services.ClientAssignmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ClientAssignmentController.class)
@Import(WebMvcTestSecurityConfig.class)
class ClientAssignmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClientAssignmentService assignmentService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Nested
    class GetAssignedUsers {

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturnAssignments() throws Exception {
            when(assignmentService.getAssignedUsers(1L)).thenReturn(new AssignmentResponse(1L, List.of(100L, 101L)));

            mockMvc.perform(get("/api/clients/1/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.clientId").value(1L))
                    .andExpect(jsonPath("$.userIds[0]").value(100L))
                    .andExpect(jsonPath("$.userIds[1]").value(101L));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturnNotFoundWhenClientNotFound() throws Exception {
            when(assignmentService.getAssignedUsers(99L)).thenThrow(new ResourceNotFoundException("Not found"));

            mockMvc.perform(get("/api/clients/99/users"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void shouldReturnForbiddenForManager() throws Exception {
            mockMvc.perform(get("/api/clients/1/users"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class ReplaceAssignments {

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReplaceAssignments() throws Exception {
            AssignmentRequest request = new AssignmentRequest(List.of(100L));
            when(assignmentService.replaceAssignments(eq(1L), any())).thenReturn(new AssignmentResponse(1L, List.of(100L)));

            mockMvc.perform(put("/api/clients/1/users")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.clientId").value(1L));
        }
    }

    @Nested
    class AssignUser {

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldAssignUser() throws Exception {
            mockMvc.perform(post("/api/clients/1/users/100").with(csrf()))
                    .andExpect(status().isCreated());

            verify(assignmentService).assignUser(1L, 100L);
        }
    }

    @Nested
    class RemoveUser {

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldRemoveUser() throws Exception {
            mockMvc.perform(delete("/api/clients/1/users/100").with(csrf()))
                    .andExpect(status().isNoContent());

            verify(assignmentService).removeUser(1L, 100L);
        }
    }

    @Nested
    class GetAssignedClientsForUser {

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturnClientIds() throws Exception {
            when(assignmentService.getClientsForUser(100L)).thenReturn(List.of(1L, 2L));

            mockMvc.perform(get("/api/clients/users/100/clients"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0]").value(1L))
                    .andExpect(jsonPath("$[1]").value(2L));
        }
    }
}

package clm.client.demo.controllers;

import clm.client.demo.config.WebMvcTestSecurityConfig;
import clm.client.demo.dtos.request.WorkPointRequest;
import clm.client.demo.dtos.response.WorkPointResponse;
import clm.client.demo.exceptions.ResourceNotFoundException;
import clm.client.demo.models.enums.Administration;
import clm.client.demo.services.WorkPointService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = WorkPointController.class)
@Import(WebMvcTestSecurityConfig.class)
class WorkPointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkPointService workPointService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    private WorkPointResponse sampleWorkPoint() {
        return new WorkPointResponse(10L, 1L, "Branch Office",
                LocalDate.of(2024, 1, 1), null,
                Administration.AJFP_CLUJ, true, 5, "RO123456", false);
    }

    @Nested
    class ListWorkPoints {

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnWorkPointsForClient() throws Exception {
            // Given
            when(workPointService.listWorkPoints(1L)).thenReturn(List.of(sampleWorkPoint()));

            // When / Then
            mockMvc.perform(get("/api/clients/1/work-points"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(10))
                    .andExpect(jsonPath("$[0].name").value("Branch Office"))
                    .andExpect(jsonPath("$[0].clientId").value(1));
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void shouldReturnWorkPointsForManager() throws Exception {
            // Given
            when(workPointService.listWorkPoints(1L)).thenReturn(List.of(sampleWorkPoint()));

            // When / Then
            mockMvc.perform(get("/api/clients/1/work-points"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnEmptyListWhenClientHasNoWorkPoints() throws Exception {
            // Given
            when(workPointService.listWorkPoints(99L)).thenReturn(List.of());

            // When / Then
            mockMvc.perform(get("/api/clients/99/work-points"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/clients/1/work-points"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class GetWorkPoint {

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnWorkPointWhenExists() throws Exception {
            // Given
            when(workPointService.getWorkPoint(1L, 10L)).thenReturn(sampleWorkPoint());

            // When / Then
            mockMvc.perform(get("/api/clients/1/work-points/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10))
                    .andExpect(jsonPath("$.name").value("Branch Office"))
                    .andExpect(jsonPath("$.employeeCount").value(5));
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnNotFoundWhenWorkPointDoesNotExist() throws Exception {
            // Given
            when(workPointService.getWorkPoint(1L, 99L))
                    .thenThrow(new ResourceNotFoundException("Work point not found: 99"));

            // When / Then
            mockMvc.perform(get("/api/clients/1/work-points/99"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/clients/1/work-points/10"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class CreateWorkPoint {

        private WorkPointRequest validRequest() {
            return new WorkPointRequest("Branch", LocalDate.of(2024, 1, 1), null,
                    Administration.AJFP_CLUJ, true, 5, null, false);
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void shouldCreateWorkPointAndReturn201() throws Exception {
            // Given
            when(workPointService.createWorkPoint(eq(1L), any())).thenReturn(sampleWorkPoint());

            // When / Then
            mockMvc.perform(post("/api/clients/1/work-points")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(10))
                    .andExpect(jsonPath("$.name").value("Branch Office"));

            verify(workPointService).createWorkPoint(eq(1L), any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldCreateWorkPointAsAdmin() throws Exception {
            // Given
            when(workPointService.createWorkPoint(eq(1L), any())).thenReturn(sampleWorkPoint());

            // When / Then
            mockMvc.perform(post("/api/clients/1/work-points")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnForbiddenForUserRole() throws Exception {
            mockMvc.perform(post("/api/clients/1/work-points")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
            mockMvc.perform(post("/api/clients/1/work-points")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void shouldReturnNotFoundWhenClientDoesNotExist() throws Exception {
            // Given
            when(workPointService.createWorkPoint(eq(99L), any()))
                    .thenThrow(new ResourceNotFoundException("Client not found: 99"));

            // When / Then
            mockMvc.perform(post("/api/clients/99/work-points")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class UpdateWorkPoint {

        private WorkPointRequest validRequest() {
            return new WorkPointRequest("Updated Branch", LocalDate.of(2024, 1, 1), null,
                    Administration.AJFP_CLUJ, true, 10, null, true);
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void shouldUpdateWorkPointAndReturn200() throws Exception {
            // Given
            when(workPointService.updateWorkPoint(eq(1L), eq(10L), any())).thenReturn(sampleWorkPoint());

            // When / Then
            mockMvc.perform(put("/api/clients/1/work-points/10")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10));

            verify(workPointService).updateWorkPoint(eq(1L), eq(10L), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnForbiddenForUserRole() throws Exception {
            mockMvc.perform(put("/api/clients/1/work-points/10")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void shouldReturnNotFoundWhenWorkPointDoesNotExist() throws Exception {
            // Given
            when(workPointService.updateWorkPoint(eq(1L), eq(99L), any()))
                    .thenThrow(new ResourceNotFoundException("Work point not found: 99"));

            // When / Then
            mockMvc.perform(put("/api/clients/1/work-points/99")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
            mockMvc.perform(put("/api/clients/1/work-points/10")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class DeleteWorkPoint {

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldDeleteWorkPointAndReturn204() throws Exception {
            // When / Then
            mockMvc.perform(delete("/api/clients/1/work-points/10").with(csrf()))
                    .andExpect(status().isNoContent());

            verify(workPointService).deleteWorkPoint(1L, 10L);
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void shouldReturnForbiddenForManagerRole() throws Exception {
            mockMvc.perform(delete("/api/clients/1/work-points/10").with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnForbiddenForUserRole() throws Exception {
            mockMvc.perform(delete("/api/clients/1/work-points/10").with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturnNotFoundWhenWorkPointDoesNotExist() throws Exception {
            // Given
            doThrow(new ResourceNotFoundException("Work point not found: 99"))
                    .when(workPointService).deleteWorkPoint(1L, 99L);

            // When / Then
            mockMvc.perform(delete("/api/clients/1/work-points/99").with(csrf()))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
            mockMvc.perform(delete("/api/clients/1/work-points/10"))
                    .andExpect(status().isUnauthorized());
        }
    }
}

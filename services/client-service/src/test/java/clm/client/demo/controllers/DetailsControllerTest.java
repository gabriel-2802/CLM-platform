package clm.client.demo.controllers;

import clm.client.demo.config.WebMvcTestSecurityConfig;
import clm.client.demo.dtos.request.DetailsRequest;
import clm.client.demo.dtos.response.DetailsResponse;
import clm.client.demo.exceptions.ResourceNotFoundException;
import clm.client.demo.services.DetailsService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DetailsController.class)
@Import(WebMvcTestSecurityConfig.class)
class DetailsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DetailsService detailsService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    private DetailsResponse sampleResponse() {
        return new DetailsResponse(10L, 1L, null, null, null, null, null, null, null, null, null);
    }

    @Nested
    class GetDetails {

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnDetailsWhenExists() throws Exception {
            // Given
            when(detailsService.getDetails(1L)).thenReturn(sampleResponse());

            // When / Then
            mockMvc.perform(get("/api/clients/1/details"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10L))
                    .andExpect(jsonPath("$.clientId").value(1L));

            verify(detailsService).getDetails(1L);
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnNotFoundWhenNotExists() throws Exception {
            // Given
            when(detailsService.getDetails(99L)).thenThrow(new ResourceNotFoundException("Details not found"));

            // When / Then
            mockMvc.perform(get("/api/clients/99/details"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/clients/1/details"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class CreateOrReplaceDetails {

        @Test
        @WithMockUser(roles = "MANAGER")
        void shouldCreateDetailsAndReturn201() throws Exception {
            // Given
            DetailsRequest request = new DetailsRequest(true, clm.client.demo.models.enums.YesNoNa.DA, true, true, true, true, "pass", true, clm.client.demo.models.enums.YesNoNa.DA);
            when(detailsService.upsertDetails(eq(1L), any())).thenReturn(sampleResponse());

            // When / Then
            mockMvc.perform(put("/api/clients/1/details")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(10L));

            verify(detailsService).upsertDetails(eq(1L), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnForbiddenForUserRole() throws Exception {
            DetailsRequest request = new DetailsRequest(true, clm.client.demo.models.enums.YesNoNa.DA, true, true, true, true, "pass", true, clm.client.demo.models.enums.YesNoNa.DA);
            mockMvc.perform(put("/api/clients/1/details")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
            mockMvc.perform(put("/api/clients/1/details")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class PartialUpdateDetails {

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldPatchDetailsAndReturn200() throws Exception {
            // Given
            DetailsRequest request = new DetailsRequest(true, clm.client.demo.models.enums.YesNoNa.DA, true, true, true, true, "pass", true, clm.client.demo.models.enums.YesNoNa.DA);
            when(detailsService.patchDetails(eq(1L), any())).thenReturn(sampleResponse());

            // When / Then
            mockMvc.perform(patch("/api/clients/1/details")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10L));

            verify(detailsService).patchDetails(eq(1L), any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturnNotFoundWhenDetailsNotExist() throws Exception {
            // Given
            DetailsRequest request = new DetailsRequest(true, clm.client.demo.models.enums.YesNoNa.DA, true, true, true, true, "pass", true, clm.client.demo.models.enums.YesNoNa.DA);
            when(detailsService.patchDetails(eq(99L), any())).thenThrow(new ResourceNotFoundException("Not found"));

            // When / Then
            mockMvc.perform(patch("/api/clients/99/details")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnForbiddenForUserRole() throws Exception {
            mockMvc.perform(patch("/api/clients/1/details")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());
        }
    }
}

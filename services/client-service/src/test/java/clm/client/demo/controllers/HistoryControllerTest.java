package clm.client.demo.controllers;

import clm.client.demo.config.WebMvcTestSecurityConfig;
import clm.client.demo.dtos.request.HistoryRequest;
import clm.client.demo.dtos.response.HistoryResponse;
import clm.client.demo.exceptions.ResourceNotFoundException;
import clm.client.demo.services.HistoryService;
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

@WebMvcTest(controllers = HistoryController.class)
@Import(WebMvcTestSecurityConfig.class)
class HistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HistoryService historyService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    private HistoryResponse sampleResponse() {
        return new HistoryResponse(10L, 1L, 2024, null, null, null, null);
    }

    @Nested
    class ListHistory {
        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnHistories() throws Exception {
            when(historyService.listHistory(1L)).thenReturn(List.of(sampleResponse()));

            mockMvc.perform(get("/api/clients/1/histories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(10L));
        }

        @Test
        void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/clients/1/histories"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class GetHistory {
        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnHistory() throws Exception {
            when(historyService.getHistory(1L, 2024)).thenReturn(sampleResponse());

            mockMvc.perform(get("/api/clients/1/histories/2024"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10L));
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnNotFound() throws Exception {
            when(historyService.getHistory(1L, 2024)).thenThrow(new ResourceNotFoundException("Not found"));

            mockMvc.perform(get("/api/clients/1/histories/2024"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class CreateOrReplaceHistory {
        @Test
        @WithMockUser(roles = "MANAGER")
        void shouldCreateAndReturn201() throws Exception {
            HistoryRequest request = new HistoryRequest(2024, java.math.BigDecimal.TEN, true, clm.client.demo.models.enums.YesNoNa.DA, clm.client.demo.models.enums.YesNoNa.DA);
            when(historyService.upsertHistory(eq(1L), eq(2024), any())).thenReturn(sampleResponse());

            mockMvc.perform(put("/api/clients/1/histories/2024")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(10L));
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldReturnForbiddenForUser() throws Exception {
            HistoryRequest request = new HistoryRequest(2024, java.math.BigDecimal.TEN, true, clm.client.demo.models.enums.YesNoNa.DA, clm.client.demo.models.enums.YesNoNa.DA);
            
            mockMvc.perform(put("/api/clients/1/histories/2024")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class DeleteHistory {
        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldDeleteAndReturn204() throws Exception {
            mockMvc.perform(delete("/api/clients/1/histories/2024").with(csrf()))
                    .andExpect(status().isNoContent());

            verify(historyService).deleteHistory(1L, 2024);
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void shouldReturnForbiddenForManager() throws Exception {
            mockMvc.perform(delete("/api/clients/1/histories/2024").with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }
}

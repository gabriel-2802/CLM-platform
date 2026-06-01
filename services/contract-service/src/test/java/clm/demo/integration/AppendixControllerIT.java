package clm.demo.integration;

import clm.demo.controllers.AppendixController;
import clm.demo.dto.responses.AppendixResponseDTO;
import clm.demo.exceptions.exceptions.InvalidAppendixStateException;
import clm.demo.exceptions.exceptions.ResourceNotFoundException;
import clm.demo.services.AppendixService;
import clm.demo.services.download.DocumentDownloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AppendixControllerIT extends AbstractControllerTest {

    @Mock AppendixService         appendixService;
    @Mock DocumentDownloadService downloadService;

    @InjectMocks AppendixController controller;

    @BeforeEach
    void setUp() {
        buildMockMvc(controller);
    }

    // ── POST /api/appendices/generate ─────────────────────────────────────────

    @Nested
    class GenerateAppendix {

        @Test
        void should_return_201_when_appendix_created() throws Exception {
            AppendixResponseDTO dto = appendixDto(10L, "DRAFT");
            when(appendixService.generateAppendix(any())).thenReturn(dto);

            mockMvc.perform(post("/api/appendices/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "contractId": 1,
                                      "templateId": 2,
                                      "title": "Amendment #1",
                                      "userId": 42,
                                      "mappings": { "Field": "Value" }
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(10))
                    .andExpect(jsonPath("$.appendixStatus").value("DRAFT"));
        }

        @Test
        void should_return_400_when_contract_id_missing() throws Exception {
            mockMvc.perform(post("/api/appendices/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "templateId": 2,
                                      "title": "Amendment",
                                      "mappings": { "Field": "Value" }
                                    }
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void should_return_400_when_title_is_blank() throws Exception {
            mockMvc.perform(post("/api/appendices/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "contractId": 1,
                                      "templateId": 2,
                                      "title": "",
                                      "mappings": { "Field": "Value" }
                                    }
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void should_return_404_when_contract_not_found() throws Exception {
            when(appendixService.generateAppendix(any()))
                    .thenThrow(new ResourceNotFoundException("Contract not found: 99"));

            mockMvc.perform(post("/api/appendices/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "contractId": 99,
                                      "templateId": 2,
                                      "title": "Test",
                                      "mappings": { "Field": "Value" }
                                    }
                                    """))
                    .andExpect(status().isNotFound());
        }
    }

    // ── POST /api/appendices/{id}/upload-signed ────────────────────────────────

    @Nested
    class UploadSignedAppendix {

        @Test
        void should_return_200_when_signed_document_uploaded() throws Exception {
            AppendixResponseDTO dto = appendixDto(1L, "SIGNED");
            when(appendixService.uploadSignedAppendix(eq(1L), any(), any())).thenReturn(dto);

            MockMultipartFile file = new MockMultipartFile(
                    "file", "signed.pdf", "application/pdf",
                    new byte[]{0x25, 0x50, 0x44, 0x46});

            mockMvc.perform(multipart("/api/appendices/1/upload-signed")
                            .file(file)
                            .param("userId", "42"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.appendixStatus").value("SIGNED"));
        }

        @Test
        void should_return_400_when_file_is_empty() throws Exception {
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file", "empty.pdf", "application/pdf", new byte[0]);

            mockMvc.perform(multipart("/api/appendices/1/upload-signed")
                            .file(emptyFile))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void should_return_404_when_appendix_not_found() throws Exception {
            when(appendixService.uploadSignedAppendix(eq(99L), any(), any()))
                    .thenThrow(new ResourceNotFoundException("Appendix not found: 99"));

            MockMultipartFile file = new MockMultipartFile(
                    "file", "signed.pdf", "application/pdf",
                    new byte[]{0x25, 0x50, 0x44, 0x46});

            mockMvc.perform(multipart("/api/appendices/99/upload-signed")
                            .file(file))
                    .andExpect(status().isNotFound());
        }

        @Test
        void should_return_409_when_appendix_already_signed() throws Exception {
            when(appendixService.uploadSignedAppendix(eq(1L), any(), any()))
                    .thenThrow(new InvalidAppendixStateException("Appendix 1 is already SIGNED"));

            MockMultipartFile file = new MockMultipartFile(
                    "file", "signed.pdf", "application/pdf",
                    new byte[]{0x25, 0x50, 0x44, 0x46});

            mockMvc.perform(multipart("/api/appendices/1/upload-signed")
                            .file(file))
                    .andExpect(status().isConflict());
        }
    }

    // ── GET /api/appendices/contract/{contractId} ─────────────────────────────

    @Nested
    class GetAppendicesForContract {

        @Test
        void should_return_200_with_list_when_appendices_exist() throws Exception {
            List<AppendixResponseDTO> appendices = List.of(
                    appendixDto(1L, "DRAFT"),
                    appendixDto(2L, "SIGNED")
            );
            when(appendixService.getAppendicesForContract(1L)).thenReturn(appendices);

            mockMvc.perform(get("/api/appendices/contract/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        void should_return_204_when_no_appendices() throws Exception {
            when(appendixService.getAppendicesForContract(1L)).thenReturn(List.of());

            mockMvc.perform(get("/api/appendices/contract/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        void should_return_404_when_contract_not_found() throws Exception {
            when(appendixService.getAppendicesForContract(99L))
                    .thenThrow(new ResourceNotFoundException("Contract not found: 99"));

            mockMvc.perform(get("/api/appendices/contract/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // ── PATCH /api/appendices/{id}/terminate ──────────────────────────────────

    @Nested
    class TerminateAppendix {

        @Test
        void should_return_200_when_terminated_successfully() throws Exception {
            AppendixResponseDTO dto = appendixDto(1L, "TERMINATED");
            when(appendixService.terminateAppendix(1L)).thenReturn(dto);

            mockMvc.perform(patch("/api/appendices/1/terminate"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.appendixStatus").value("TERMINATED"));
        }

        @Test
        void should_return_404_when_appendix_not_found() throws Exception {
            when(appendixService.terminateAppendix(99L))
                    .thenThrow(new ResourceNotFoundException("Appendix not found: 99"));

            mockMvc.perform(patch("/api/appendices/99/terminate"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void should_return_409_when_appendix_not_signed() throws Exception {
            when(appendixService.terminateAppendix(1L))
                    .thenThrow(new InvalidAppendixStateException("only SIGNED appendices can be terminated"));

            mockMvc.perform(patch("/api/appendices/1/terminate"))
                    .andExpect(status().isConflict());
        }
    }

    // ── DELETE /api/appendices/{id} ───────────────────────────────────────────

    @Nested
    class DeleteAppendix {

        @Test
        void should_return_204_when_deleted_successfully() throws Exception {
            doNothing().when(appendixService).deleteAppendix(1L);

            mockMvc.perform(delete("/api/appendices/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        void should_return_404_when_appendix_not_found() throws Exception {
            doThrow(new ResourceNotFoundException("Appendix not found: 99"))
                    .when(appendixService).deleteAppendix(99L);

            mockMvc.perform(delete("/api/appendices/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // ── GET /api/appendices/download/{id}/{type}/{format} ─────────────────────

    @Nested
    class DownloadAppendix {

        @Test
        void should_return_200_with_bytes_when_download_succeeds() throws Exception {
            byte[] content = new byte[]{1, 2, 3};
            when(downloadService.downloadDocument(eq(1L), any(), any())).thenReturn(content);

            mockMvc.perform(get("/api/appendices/download/1/unsigned/pdf"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition",
                            org.hamcrest.Matchers.containsString("appendix-1.pdf")));
        }

        @Test
        void should_return_400_when_format_invalid() throws Exception {
            mockMvc.perform(get("/api/appendices/download/1/unsigned/exe"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void should_return_400_when_type_invalid() throws Exception {
            mockMvc.perform(get("/api/appendices/download/1/raw/pdf"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static AppendixResponseDTO appendixDto(Long id, String status) {
        AppendixResponseDTO dto = new AppendixResponseDTO();
        dto.setId(id);
        dto.setAppendixStatus(status);
        return dto;
    }
}

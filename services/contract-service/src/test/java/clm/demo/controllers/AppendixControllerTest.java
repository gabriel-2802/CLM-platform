package clm.demo.controllers;

import clm.demo.dto.responses.AppendixResponseDTO;
import clm.demo.exceptions.GlobalExceptionHandler;
import clm.demo.exceptions.exceptions.InvalidAppendixStateException;
import clm.demo.exceptions.exceptions.ResourceNotFoundException;
import clm.demo.exceptions.exceptions.TemplateIncompleteException;
import clm.demo.services.AppendixService;
import clm.demo.services.download.DocumentDownloadService;
import clm.demo.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AppendixControllerTest {

    @Mock AppendixService         appendixService;
    @Mock DocumentDownloadService downloadService;

    @InjectMocks AppendixController controller;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    // ================================================================== //
    //  POST /api/appendices/generate                                       //
    // ================================================================== //

    @Nested
    class GenerateAppendix {

        @Test
        void valid_request_returns_201_with_body() throws Exception {
            AppendixResponseDTO dto = TestDataFactory.appendixResponse(12L, "DRAFT");
            when(appendixService.generateAppendix(any())).thenReturn(dto);

            String body = """
                {
                  "contractId": 1,
                  "templateId": 1,
                  "title": "Exhibit A",
                  "mappings": { "Client Name": "Acme Corp" }
                }
                """;

            mockMvc.perform(post("/api/appendices/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(12))
                    .andExpect(jsonPath("$.appendixStatus").value("DRAFT"));
        }

        @Test
        void missing_required_fields_returns_400() throws Exception {
            // empty body — @NotNull, @NotBlank, @NotEmpty fields all fail
            mockMvc.perform(post("/api/appendices/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void contract_not_found_returns_404() throws Exception {
            when(appendixService.generateAppendix(any()))
                    .thenThrow(new ResourceNotFoundException("Contract not found: 99"));

            String body = """
                {
                  "contractId": 99,
                  "templateId": 1,
                  "title": "Exhibit A",
                  "mappings": { "Client Name": "Acme Corp" }
                }
                """;

            mockMvc.perform(post("/api/appendices/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound());
        }

        @Test
        void template_not_fully_mapped_returns_422() throws Exception {
            when(appendixService.generateAppendix(any()))
                    .thenThrow(new TemplateIncompleteException("Template is not fully mapped"));

            String body = """
                {
                  "contractId": 1,
                  "templateId": 1,
                  "title": "Exhibit A",
                  "mappings": { "Client Name": "Acme Corp" }
                }
                """;

            mockMvc.perform(post("/api/appendices/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnprocessableEntity());
        }
    }

    // ================================================================== //
    //  POST /api/appendices/upload                                         //
    // ================================================================== //

    @Nested
    class UploadDirectAppendix {

        @Test
        void valid_pdf_returns_201_with_signed_status() throws Exception {
            AppendixResponseDTO dto = TestDataFactory.appendixResponse(13L, "SIGNED");
            when(appendixService.uploadDirectAppendix(any())).thenReturn(dto);

            MockMultipartFile file = new MockMultipartFile(
                    "file", "exhibit.pdf", "application/pdf", TestDataFactory.pdfMagicBytes());

            mockMvc.perform(multipart("/api/appendices/upload")
                            .file(file)
                            .param("contractId", "1")
                            .param("title", "Exhibit A"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.appendixStatus").value("SIGNED"));
        }

        @Test
        void empty_file_returns_400() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "empty.pdf", "application/pdf", new byte[0]);

            mockMvc.perform(multipart("/api/appendices/upload")
                            .file(file)
                            .param("contractId", "1")
                            .param("title", "Exhibit A"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void missing_contract_id_returns_400() throws Exception {
            // omitting contractId triggers @NotNull on UploadDirectAppendixRequest
            MockMultipartFile file = new MockMultipartFile(
                    "file", "exhibit.pdf", "application/pdf", TestDataFactory.pdfMagicBytes());

            mockMvc.perform(multipart("/api/appendices/upload")
                            .file(file)
                            .param("title", "Exhibit A"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void contract_not_found_returns_404() throws Exception {
            when(appendixService.uploadDirectAppendix(any()))
                    .thenThrow(new ResourceNotFoundException("Contract not found: 99"));

            MockMultipartFile file = new MockMultipartFile(
                    "file", "exhibit.pdf", "application/pdf", TestDataFactory.pdfMagicBytes());

            mockMvc.perform(multipart("/api/appendices/upload")
                            .file(file)
                            .param("contractId", "99")
                            .param("title", "Exhibit A"))
                    .andExpect(status().isNotFound());
        }
    }

    // ================================================================== //
    //  POST /api/appendices/{appendixId}/upload-signed                     //
    // ================================================================== //

    @Nested
    class UploadSignedAppendix {

        @Test
        void draft_appendix_returns_200_with_signed_status() throws Exception {
            AppendixResponseDTO dto = TestDataFactory.appendixResponse(12L, "SIGNED");
            when(appendixService.uploadSignedAppendix(eq(12L), any())).thenReturn(dto);

            MockMultipartFile file = new MockMultipartFile(
                    "file", "signed.pdf", "application/pdf", TestDataFactory.pdfMagicBytes());

            mockMvc.perform(multipart("/api/appendices/12/upload-signed").file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.appendixStatus").value("SIGNED"));
        }

        @Test
        void empty_file_returns_400() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "empty.pdf", "application/pdf", new byte[0]);

            mockMvc.perform(multipart("/api/appendices/12/upload-signed").file(file))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void appendix_not_found_returns_404() throws Exception {
            when(appendixService.uploadSignedAppendix(eq(99L), any()))
                    .thenThrow(new ResourceNotFoundException("Appendix not found: 99"));

            MockMultipartFile file = new MockMultipartFile(
                    "file", "signed.pdf", "application/pdf", TestDataFactory.pdfMagicBytes());

            mockMvc.perform(multipart("/api/appendices/99/upload-signed").file(file))
                    .andExpect(status().isNotFound());
        }

        @Test
        void already_signed_appendix_returns_409() throws Exception {
            when(appendixService.uploadSignedAppendix(eq(12L), any()))
                    .thenThrow(new InvalidAppendixStateException("Appendix 12 is already SIGNED"));

            MockMultipartFile file = new MockMultipartFile(
                    "file", "signed.pdf", "application/pdf", TestDataFactory.pdfMagicBytes());

            mockMvc.perform(multipart("/api/appendices/12/upload-signed").file(file))
                    .andExpect(status().isConflict());
        }
    }

    // ================================================================== //
    //  GET /api/appendices/contract/{contractId}                           //
    // ================================================================== //

    @Nested
    class GetAppendicesForContract {

        @Test
        void returns_200_with_list() throws Exception {
            AppendixResponseDTO dto = TestDataFactory.appendixResponse(12L, "DRAFT");
            when(appendixService.getAppendicesForContract(1L)).thenReturn(List.of(dto));

            mockMvc.perform(get("/api/appendices/contract/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(12))
                    .andExpect(jsonPath("$[0].appendixStatus").value("DRAFT"));
        }

        @Test
        void empty_list_returns_204() throws Exception {
            when(appendixService.getAppendicesForContract(1L)).thenReturn(List.of());

            mockMvc.perform(get("/api/appendices/contract/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        void contract_not_found_returns_404() throws Exception {
            when(appendixService.getAppendicesForContract(99L))
                    .thenThrow(new ResourceNotFoundException("Contract not found: 99"));

            mockMvc.perform(get("/api/appendices/contract/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // ================================================================== //
    //  DELETE /api/appendices/{appendixId}                                 //
    // ================================================================== //

    @Nested
    class DeleteAppendix {

        @Test
        void successful_delete_returns_204() throws Exception {
            doNothing().when(appendixService).deleteAppendix(12L);

            mockMvc.perform(delete("/api/appendices/12"))
                    .andExpect(status().isNoContent());
        }

        @Test
        void not_found_returns_404() throws Exception {
            doThrow(new ResourceNotFoundException("Appendix not found: 99"))
                    .when(appendixService).deleteAppendix(99L);

            mockMvc.perform(delete("/api/appendices/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // ================================================================== //
    //  GET /api/appendices/download/{appendixId}/{type}/{format}           //
    // ================================================================== //

    @Nested
    class DownloadAppendix {

        @Test
        void valid_request_returns_200_with_attachment_header() throws Exception {
            when(downloadService.downloadDocument(any(), any(), any()))
                    .thenReturn(new byte[]{1, 2, 3});

            mockMvc.perform(get("/api/appendices/download/12/unsigned/pdf"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition",
                            "attachment; filename=appendix-12.pdf"));
        }

        @Test
        void invalid_format_returns_400() throws Exception {
            mockMvc.perform(get("/api/appendices/download/12/unsigned/xlsx"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void invalid_type_returns_400() throws Exception {
            mockMvc.perform(get("/api/appendices/download/12/original/pdf"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void appendix_not_found_returns_404() throws Exception {
            when(downloadService.downloadDocument(any(), any(), any()))
                    .thenThrow(new ResourceNotFoundException("not found"));

            mockMvc.perform(get("/api/appendices/download/99/unsigned/pdf"))
                    .andExpect(status().isNotFound());
        }
    }
}

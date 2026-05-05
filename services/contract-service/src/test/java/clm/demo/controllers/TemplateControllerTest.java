package clm.demo.controllers;

import clm.demo.dto.responses.TemplateFieldResponseDTO;
import clm.demo.dto.responses.TemplateResponseDTO;
import clm.demo.dto.responses.TemplateUploadResponseDTO;
import clm.demo.exceptions.exceptions.DuplicateTemplateNameException;
import clm.demo.exceptions.GlobalExceptionHandler;
import clm.demo.exceptions.exceptions.ResourceNotFoundException;
import clm.demo.services.TemplateService;
import clm.demo.services.download.DocumentDownloadService;
import clm.demo.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TemplateControllerTest {

    @Mock TemplateService         templateService;
    @Mock DocumentDownloadService downloadService;

    @InjectMocks TemplateController controller;

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
    //  POST /api/templates/upload                                          //
    // ================================================================== //

    @Nested
    class UploadTemplate {

        @Test
        void valid_docx_returns_201_with_body() throws Exception {
            TemplateUploadResponseDTO dto = TestDataFactory.uploadResponse(1L, "NDA");
            when(templateService.uploadTemplate(any())).thenReturn(dto);

            MockMultipartFile file = new MockMultipartFile(
                    "file", "nda.docx",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    new byte[]{1, 2, 3});

            mockMvc.perform(multipart("/api/templates/upload")
                            .file(file)
                            .param("templateName", "NDA")
                            .param("description", "test"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.templateId").value(1))
                    .andExpect(jsonPath("$.templateName").value("NDA"));
        }

        @Test
        void duplicate_name_returns_409() throws Exception {
            when(templateService.uploadTemplate(any()))
                    .thenThrow(new DuplicateTemplateNameException("NDA already exists"));

            MockMultipartFile file = new MockMultipartFile(
                    "file", "nda.docx", "application/octet-stream", new byte[]{1, 2, 3});

            mockMvc.perform(multipart("/api/templates/upload")
                            .file(file)
                            .param("templateName", "NDA"))
                    .andExpect(status().isConflict());
        }
    }

    // ================================================================== //
    //  GET /api/templates                                                  //
    // ================================================================== //

    @Nested
    class GetAllTemplates {

        @Test
        void returns_200_with_list() throws Exception {
            TemplateResponseDTO dto = TestDataFactory.templateResponse(1L, "NDA");
            when(templateService.getAllTemplates(anyInt(), anyInt()))
                    .thenReturn(new PageImpl<>(List.of(dto)));

            mockMvc.perform(get("/api/templates"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].templateId").value(1));
        }

        @Test
        void empty_page_returns_204() throws Exception {
            when(templateService.getAllTemplates(anyInt(), anyInt()))
                    .thenReturn(new PageImpl<>(List.of()));

            mockMvc.perform(get("/api/templates"))
                    .andExpect(status().isNoContent());
        }
    }

    // ================================================================== //
    //  GET /api/templates/{id}                                             //
    // ================================================================== //

    @Nested
    class GetTemplate {

        @Test
        void found_returns_200() throws Exception {
            when(templateService.getTemplate(1L))
                    .thenReturn(TestDataFactory.templateResponse(1L, "NDA"));

            mockMvc.perform(get("/api/templates/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.templateName").value("NDA"));
        }

        @Test
        void not_found_returns_404() throws Exception {
            when(templateService.getTemplate(99L))
                    .thenThrow(new ResourceNotFoundException("not found"));

            mockMvc.perform(get("/api/templates/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // ================================================================== //
    //  PUT /api/templates/{id}/labels                                      //
    // ================================================================== //

    @Nested
    class UpdateFieldLabels {

        @Test
        void valid_request_returns_200_with_updated_fields() throws Exception {
            List<TemplateFieldResponseDTO> fields = List.of(
                    TestDataFactory.fieldResponse(10L, "Client Name"));
            when(templateService.updateFieldLabels(any())).thenReturn(fields);

            String body = """
                {
                  "templateId": 1,
                  "mappings": [
                    { "fieldId": 10, "fieldLabel": "Client Name", "dataType": "STRING", "isRequired": true, "formatPattern": "" }
                  ]
                }
                """;

            mockMvc.perform(put("/api/templates/1/labels")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk());
        }

        @Test
        void missing_template_id_returns_400() throws Exception {
            // body with null templateId — @NotNull validation should reject it
            String body = """
                { "templateId": null, "mappings": [] }
                """;

            mockMvc.perform(put("/api/templates/1/labels")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    // ================================================================== //
    //  DELETE /api/templates/{id}                                          //
    // ================================================================== //

    @Nested
    class DeleteTemplate {

        @Test
        void successful_delete_returns_204() throws Exception {
            doNothing().when(templateService).deleteTemplate(1L);

            mockMvc.perform(delete("/api/templates/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        void not_found_returns_404() throws Exception {
            doThrow(new ResourceNotFoundException("not found"))
                    .when(templateService).deleteTemplate(99L);

            mockMvc.perform(delete("/api/templates/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // ================================================================== //
    //  GET /api/templates/download/{id}/{format}                           //
    // ================================================================== //

    @Nested
    class DownloadTemplate {

        @Test
        void valid_format_returns_200_with_file() throws Exception {
            when(downloadService.downloadDocument(any(), any(), any()))
                    .thenReturn(new byte[]{1, 2, 3});

            mockMvc.perform(get("/api/templates/download/1/pdf"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition",
                            "attachment; filename=template-1.pdf"));
        }

        @Test
        void invalid_format_returns_400() throws Exception {
            mockMvc.perform(get("/api/templates/download/1/xlsx"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void not_found_template_returns_404() throws Exception {
            when(downloadService.downloadDocument(any(), any(), any()))
                    .thenThrow(new ResourceNotFoundException("not found"));

            mockMvc.perform(get("/api/templates/download/99/pdf"))
                    .andExpect(status().isNotFound());
        }
    }
}

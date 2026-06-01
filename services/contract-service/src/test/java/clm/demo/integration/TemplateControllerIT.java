package clm.demo.integration;

import clm.demo.controllers.TemplateController;
import clm.demo.dto.responses.TemplateResponseDTO;
import clm.demo.exceptions.exceptions.DuplicateTemplateNameException;
import clm.demo.exceptions.exceptions.ResourceNotFoundException;
import clm.demo.exceptions.exceptions.TemplateFieldOwnershipException;
import clm.demo.services.TemplateService;
import clm.demo.services.download.DocumentDownloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TemplateControllerIT extends AbstractControllerTest {

    @Mock TemplateService         templateService;
    @Mock DocumentDownloadService downloadService;

    @InjectMocks TemplateController controller;

    @BeforeEach
    void setUp() {
        buildMockMvc(controller);
    }

    // ── GET /api/templates ────────────────────────────────────────────────────

    @Nested
    class GetAllTemplates {

        @Test
        void should_return_200_with_templates_when_exist() throws Exception {
            TemplateResponseDTO dto = new TemplateResponseDTO();
            dto.setTemplateId(1L);
            dto.setTemplateName("Contract A");
            when(templateService.getAllTemplates(0, 20))
                    .thenReturn(new PageImpl<>(List.of(dto)));

            mockMvc.perform(get("/api/templates"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].templateName").value("Contract A"))
                    .andExpect(jsonPath("$[0].templateId").value(1));
        }

        @Test
        void should_return_204_when_no_templates_exist() throws Exception {
            when(templateService.getAllTemplates(0, 20)).thenReturn(Page.empty());

            mockMvc.perform(get("/api/templates"))
                    .andExpect(status().isNoContent());
        }

        @Test
        void should_pass_custom_pagination_parameters() throws Exception {
            when(templateService.getAllTemplates(2, 5)).thenReturn(Page.empty());

            mockMvc.perform(get("/api/templates?page=2&size=5"))
                    .andExpect(status().isNoContent());
        }
    }

    // ── GET /api/templates/{id} ───────────────────────────────────────────────

    @Nested
    class GetTemplate {

        @Test
        void should_return_200_when_template_found() throws Exception {
            TemplateResponseDTO dto = new TemplateResponseDTO();
            dto.setTemplateId(7L);
            dto.setTemplateName("NDA Template");
            when(templateService.getTemplate(7L)).thenReturn(dto);

            mockMvc.perform(get("/api/templates/7"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.templateId").value(7))
                    .andExpect(jsonPath("$.templateName").value("NDA Template"));
        }

        @Test
        void should_return_404_when_template_not_found() throws Exception {
            when(templateService.getTemplate(99L))
                    .thenThrow(new ResourceNotFoundException("Template not found: 99"));

            mockMvc.perform(get("/api/templates/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // ── DELETE /api/templates/{id} ────────────────────────────────────────────

    @Nested
    class DeleteTemplate {

        @Test
        void should_return_204_when_deleted_successfully() throws Exception {
            doNothing().when(templateService).deleteTemplate(1L);

            mockMvc.perform(delete("/api/templates/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        void should_return_404_when_template_not_found() throws Exception {
            doThrow(new ResourceNotFoundException("Template not found: 99"))
                    .when(templateService).deleteTemplate(99L);

            mockMvc.perform(delete("/api/templates/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // ── PUT /api/templates/{id}/labels ────────────────────────────────────────

    @Nested
    class UpdateFieldLabels {

        @Test
        void should_return_200_when_labels_updated() throws Exception {
            when(templateService.updateFieldLabels(any())).thenReturn(List.of());

            mockMvc.perform(put("/api/templates/1/labels")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "templateId": 1,
                                      "mappings": [
                                        { "fieldId": 10, "fieldLabel": "CompanyName", "dataType": "STRING", "isRequired": true }
                                      ]
                                    }
                                    """))
                    .andExpect(status().isOk());
        }

        @Test
        void should_return_400_when_request_invalid() throws Exception {
            mockMvc.perform(put("/api/templates/1/labels")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "templateId": 1, "mappings": [] }
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void should_return_404_when_template_not_found() throws Exception {
            when(templateService.updateFieldLabels(any()))
                    .thenThrow(new ResourceNotFoundException("Template not found: 99"));

            mockMvc.perform(put("/api/templates/99/labels")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "templateId": 99,
                                      "mappings": [
                                        { "fieldId": 1, "fieldLabel": "Name", "dataType": "STRING", "isRequired": true }
                                      ]
                                    }
                                    """))
                    .andExpect(status().isNotFound());
        }

        @Test
        void should_return_400_when_field_does_not_belong_to_template() throws Exception {
            when(templateService.updateFieldLabels(any()))
                    .thenThrow(new TemplateFieldOwnershipException("Field 5 does not belong to template 1"));

            mockMvc.perform(put("/api/templates/1/labels")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "templateId": 1,
                                      "mappings": [
                                        { "fieldId": 5, "fieldLabel": "Name", "dataType": "STRING", "isRequired": true }
                                      ]
                                    }
                                    """))
                    .andExpect(status().isBadRequest());
        }
    }

    // ── GET /api/templates/download/{id}/{format} ─────────────────────────────

    @Nested
    class DownloadTemplate {

        @Test
        void should_return_200_with_file_bytes() throws Exception {
            byte[] content = new byte[]{1, 2, 3, 4};
            when(downloadService.downloadDocument(eq(7L), any(), any())).thenReturn(content);

            mockMvc.perform(get("/api/templates/download/7/docx"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition",
                            org.hamcrest.Matchers.containsString("template-7.docx")))
                    .andExpect(content().bytes(content));
        }

        @Test
        void should_return_400_when_format_invalid() throws Exception {
            mockMvc.perform(get("/api/templates/download/7/xyz"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void should_return_404_when_template_not_found() throws Exception {
            when(downloadService.downloadDocument(eq(99L), any(), any()))
                    .thenThrow(new ResourceNotFoundException("Template not found: 99"));

            mockMvc.perform(get("/api/templates/download/99/pdf"))
                    .andExpect(status().isNotFound());
        }
    }
}

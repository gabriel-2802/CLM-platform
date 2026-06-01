package clm.demo.unit.service;

import clm.demo.dto.requests.FieldMappingRequest;
import clm.demo.dto.requests.UploadTemplateRequest;
import clm.demo.dto.responses.TemplateFieldResponseDTO;
import clm.demo.dto.responses.TemplateResponseDTO;
import clm.demo.exceptions.exceptions.DuplicateTemplateNameException;
import clm.demo.exceptions.exceptions.ResourceNotFoundException;
import clm.demo.exceptions.exceptions.TemplateFieldOwnershipException;
import clm.demo.mappers.DocumentTemplateMapper;
import clm.demo.models.DocumentTemplate;
import clm.demo.models.TemplateField;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.repositories.DocumentTemplateRepository;
import clm.demo.repositories.TemplateFieldRepository;
import clm.demo.services.TemplateService;
import clm.demo.utils.file.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class TemplateServiceTest {

    @Mock DocumentTemplateRepository templateRepository;
    @Mock TemplateFieldRepository    templateFieldRepository;
    @Mock DocumentTemplateMapper     templateMapper;
    @Mock FileUtils                  fileUtils;

    @InjectMocks TemplateService service;

    // ── getTemplate ───────────────────────────────────────────────────────────

    @Nested
    class GetTemplate {

        @Test
        void should_return_dto_when_template_exists() {
            DocumentTemplate template = sampleTemplate(1L);
            TemplateResponseDTO dto = new TemplateResponseDTO();
            when(templateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(templateMapper.toResponseDTO(template)).thenReturn(dto);

            TemplateResponseDTO result = service.getTemplate(1L);

            assertThat(result).isSameAs(dto);
        }

        @Test
        void should_throw_when_template_not_found() {
            when(templateRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getTemplate(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // ── getAllTemplates ────────────────────────────────────────────────────────

    @Nested
    class GetAllTemplates {

        @Test
        void should_return_page_of_templates() {
            DocumentTemplate template = sampleTemplate(1L);
            TemplateResponseDTO dto = new TemplateResponseDTO();
            when(templateRepository.findAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(template)));
            when(templateMapper.toResponseDTO(template)).thenReturn(dto);

            Page<TemplateResponseDTO> result = service.getAllTemplates(0, 20);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        void should_return_empty_page_when_no_templates_exist() {
            when(templateRepository.findAll(any(Pageable.class)))
                    .thenReturn(Page.empty());

            Page<TemplateResponseDTO> result = service.getAllTemplates(0, 20);

            assertThat(result.isEmpty()).isTrue();
        }
    }

    // ── deleteTemplate ────────────────────────────────────────────────────────

    @Nested
    class DeleteTemplate {

        @Test
        void should_delete_existing_template() {
            when(templateRepository.existsById(1L)).thenReturn(true);

            service.deleteTemplate(1L);

            verify(templateRepository).deleteById(1L);
        }

        @Test
        void should_throw_when_template_not_found() {
            when(templateRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> service.deleteTemplate(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");

            verify(templateRepository, never()).deleteById(anyLong());
        }
    }

    // ── uploadTemplate ────────────────────────────────────────────────────────

    @Nested
    class UploadTemplate {

        @Test
        void should_throw_when_template_name_already_exists() throws IOException {
            MultipartFile file = docxFile("content");
            UploadTemplateRequest request = new UploadTemplateRequest();
            request.setFile(file);
            request.setTemplateName("Existing");

            when(templateRepository.findByTemplateName("Existing"))
                    .thenReturn(Optional.of(sampleTemplate(1L)));

            assertThatThrownBy(() -> service.uploadTemplate(request))
                    .isInstanceOf(DuplicateTemplateNameException.class)
                    .hasMessageContaining("Existing");
        }

        @Test
        void should_throw_when_file_is_empty() {
            MultipartFile emptyFile = new MockMultipartFile(
                    "file", "test.docx",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    new byte[0]);
            UploadTemplateRequest request = new UploadTemplateRequest();
            request.setFile(emptyFile);
            request.setTemplateName("NewTemplate");

            assertThatThrownBy(() -> service.uploadTemplate(request))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ── updateFieldLabels ─────────────────────────────────────────────────────

    @Nested
    class UpdateFieldLabels {

        @Test
        void should_throw_when_template_not_found() {
            FieldMappingRequest request = new FieldMappingRequest();
            request.setTemplateId(99L);
            request.setMappings(List.of());

            when(templateRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> service.updateFieldLabels(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        void should_throw_when_field_does_not_belong_to_template() {
            DocumentTemplate template = sampleTemplate(1L);
            DocumentTemplate otherTemplate = sampleTemplate(2L);

            TemplateField field = TemplateField.builder()
                    .documentTemplate(otherTemplate)
                    .fieldPosition(0)
                    .isRequired(true)
                    .build();
            setId(field, 10L);

            FieldMappingRequest.FieldMappingDefinition mapping =
                    new FieldMappingRequest.FieldMappingDefinition();
            mapping.setFieldId(10L);
            mapping.setFieldLabel("Name");
            mapping.setDataType("STRING");
            mapping.setRequired(true);

            FieldMappingRequest request = new FieldMappingRequest();
            request.setTemplateId(1L);
            request.setMappings(List.of(mapping));

            when(templateRepository.existsById(1L)).thenReturn(true);
            when(templateFieldRepository.findAllById(any()))
                    .thenReturn(List.of(field));

            assertThatThrownBy(() -> service.updateFieldLabels(request))
                    .isInstanceOf(TemplateFieldOwnershipException.class);
        }

        @Test
        void should_update_labels_and_set_fully_mapped_when_all_fields_have_labels() {
            DocumentTemplate template = sampleTemplate(1L);
            TemplateField field = TemplateField.builder()
                    .documentTemplate(template)
                    .fieldPosition(0)
                    .isRequired(true)
                    .build();
            setId(field, 5L);
            template.getTemplateFields().add(field);
            template.setFieldCount(1);

            FieldMappingRequest.FieldMappingDefinition mapping =
                    new FieldMappingRequest.FieldMappingDefinition();
            mapping.setFieldId(5L);
            mapping.setFieldLabel("CompanyName");
            mapping.setDataType("STRING");
            mapping.setRequired(true);

            FieldMappingRequest request = new FieldMappingRequest();
            request.setTemplateId(1L);
            request.setMappings(List.of(mapping));

            when(templateRepository.existsById(1L)).thenReturn(true);
            when(templateFieldRepository.findAllById(any())).thenReturn(List.of(field));
            when(templateFieldRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
            when(templateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(templateRepository.save(any())).thenReturn(template);

            List<TemplateFieldResponseDTO> result = service.updateFieldLabels(request);

            assertThat(result).hasSize(1);
            assertThat(template.getIsFullyMapped()).isTrue();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static DocumentTemplate sampleTemplate(Long id) {
        DocumentTemplate t = DocumentTemplate.builder()
                .templateName("Template-" + id)
                .documentFormat(DocumentFormat.DOCX)
                .documentContent(new byte[]{1, 2, 3})
                .fieldCount(0)
                .isFullyMapped(false)
                .templateFields(new ArrayList<>())
                .build();
        setId(t, id);
        return t;
    }

    private static MultipartFile docxFile(String content) {
        return new MockMultipartFile(
                "file", "test.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                content.getBytes());
    }

    private static void setId(Object entity, Long id) {
        Class<?> clazz = entity.getClass();
        while (clazz != null) {
            try {
                var f = clazz.getDeclaredField("id");
                f.setAccessible(true);
                f.set(entity, id);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

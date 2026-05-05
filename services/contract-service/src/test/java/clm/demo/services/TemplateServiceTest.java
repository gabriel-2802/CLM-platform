package clm.demo.services;

import clm.demo.dto.requests.FieldMappingRequest;
import clm.demo.dto.responses.TemplateFieldResponseDTO;
import clm.demo.dto.responses.TemplateResponseDTO;
import clm.demo.exceptions.exceptions.ResourceNotFoundException;
import clm.demo.exceptions.exceptions.TemplateFieldOwnershipException;
import clm.demo.mappers.DocumentTemplateMapper;
import clm.demo.models.DocumentTemplate;
import clm.demo.models.TemplateField;
import clm.demo.models.enums.DataType;
import clm.demo.repositories.DocumentTemplateRepository;
import clm.demo.repositories.TemplateFieldRepository;
import clm.demo.support.TestDataFactory;
import clm.demo.utils.file.FileUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TemplateServiceTest {

    @Mock DocumentTemplateRepository templateRepository;
    @Mock TemplateFieldRepository    templateFieldRepository;
    @Mock DocumentTemplateMapper     templateMapper;
    @Mock FileUtils                  fileUtils;

    @InjectMocks TemplateService service;

    // ================================================================== //
    //  getTemplate                                                         //
    // ================================================================== //

    @Nested
    class GetTemplate {

        @Test
        void found_returns_mapped_dto() {
            DocumentTemplate template = TestDataFactory.templateWithId(1L, "NDA");
            TemplateResponseDTO dto   = TestDataFactory.templateResponse(1L, "NDA");

            when(templateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(templateMapper.toResponseDTO(template)).thenReturn(dto);

            TemplateResponseDTO result = service.getTemplate(1L);

            assertThat(result.getTemplateId()).isEqualTo(1L);
            assertThat(result.getTemplateName()).isEqualTo("NDA");
        }

        @Test
        void not_found_throws_resource_not_found_exception() {
            when(templateRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getTemplate(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // ================================================================== //
    //  getAllTemplates                                                      //
    // ================================================================== //

    @Nested
    class GetAllTemplates {

        @Test
        void delegates_to_repository_with_paging_and_sort() {
            DocumentTemplate t = TestDataFactory.templateWithId(1L, "NDA");
            TemplateResponseDTO dto = TestDataFactory.templateResponse(1L, "NDA");

            when(templateRepository.findAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(t)));
            when(templateMapper.toResponseDTO(t)).thenReturn(dto);

            var page = service.getAllTemplates(0, 20);

            assertThat(page.getContent()).hasSize(1);
        }

        @Test
        void empty_repository_returns_empty_page() {
            when(templateRepository.findAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            var page = service.getAllTemplates(0, 20);

            assertThat(page).isEmpty();
        }
    }

    // ================================================================== //
    //  deleteTemplate                                                      //
    // ================================================================== //

    @Nested
    class DeleteTemplate {

        @Test
        void existing_template_is_deleted() {
            when(templateRepository.existsById(1L)).thenReturn(true);

            service.deleteTemplate(1L);

            verify(templateRepository).deleteById(1L);
        }

        @Test
        void non_existent_template_throws_resource_not_found() {
            when(templateRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> service.deleteTemplate(99L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(templateRepository, never()).deleteById(anyLong());
        }
    }

    // ================================================================== //
    //  updateFieldLabels                                                   //
    // ================================================================== //

    @Nested
    class UpdateFieldLabels {

        @Test
        void updates_label_and_returns_saved_fields() {
            DocumentTemplate template = TestDataFactory.templateWithId(1L, "NDA");
            TemplateField field = TemplateField.builder()
                    .id(10L)
                    .fieldLabel(null)
                    .dataType(DataType.STRING)
                    .fieldPosition(0)
                    .isRequired(true)
                    .documentTemplate(template)
                    .build();
            template.setTemplateFields(new ArrayList<>(List.of(field)));

            FieldMappingRequest request = TestDataFactory.fieldMappingRequest(1L, 10L, "Client Name");

            when(templateRepository.existsById(1L)).thenReturn(true);
            when(templateFieldRepository.findAllById(any())).thenReturn(List.of(field));
            when(templateFieldRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
            when(templateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(templateRepository.save(any())).thenReturn(template);

            List<TemplateFieldResponseDTO> result = service.updateFieldLabels(request);

            assertThat(result).hasSize(1);
            assertThat(field.getFieldLabel()).isEqualTo("Client Name");
        }

        @Test
        void template_not_found_throws_resource_not_found_exception() {
            when(templateRepository.existsById(99L)).thenReturn(false);

            FieldMappingRequest request = TestDataFactory.fieldMappingRequest(99L, 10L, "Label");

            assertThatThrownBy(() -> service.updateFieldLabels(request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void field_not_found_throws_resource_not_found_exception() {
            when(templateRepository.existsById(1L)).thenReturn(true);
            when(templateFieldRepository.findAllById(any())).thenReturn(List.of()); // empty

            FieldMappingRequest request = TestDataFactory.fieldMappingRequest(1L, 10L, "Label");

            assertThatThrownBy(() -> service.updateFieldLabels(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("10");
        }

        @Test
        void field_belonging_to_different_template_throws_ownership_exception() {
            DocumentTemplate otherTemplate = TestDataFactory.templateWithId(2L, "Other");
            TemplateField field = TemplateField.builder()
                    .id(10L)
                    .fieldLabel("x")
                    .dataType(DataType.STRING)
                    .isRequired(true)
                    .documentTemplate(otherTemplate) // belongs to template 2, not 1
                    .build();

            FieldMappingRequest request = TestDataFactory.fieldMappingRequest(1L, 10L, "Label");

            when(templateRepository.existsById(1L)).thenReturn(true);
            when(templateFieldRepository.findAllById(any())).thenReturn(List.of(field));

            assertThatThrownBy(() -> service.updateFieldLabels(request))
                    .isInstanceOf(TemplateFieldOwnershipException.class);
        }

        @Test
        void fully_mapped_flag_set_when_all_fields_have_labels() {
            DocumentTemplate template = TestDataFactory.templateWithId(1L, "NDA");
            TemplateField field = TemplateField.builder()
                    .id(10L)
                    .fieldLabel("Client Name") // already labelled
                    .dataType(DataType.STRING)
                    .fieldPosition(0)
                    .isRequired(true)
                    .documentTemplate(template)
                    .build();
            template.setTemplateFields(new ArrayList<>(List.of(field)));
            template.setFieldCount(1);

            FieldMappingRequest request = TestDataFactory.fieldMappingRequest(1L, 10L, "Client Name");

            when(templateRepository.existsById(1L)).thenReturn(true);
            when(templateFieldRepository.findAllById(any())).thenReturn(List.of(field));
            when(templateFieldRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
            when(templateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(templateRepository.save(any())).thenReturn(template);

            service.updateFieldLabels(request);

            assertThat(template.getIsFullyMapped()).isTrue();
        }
    }
}

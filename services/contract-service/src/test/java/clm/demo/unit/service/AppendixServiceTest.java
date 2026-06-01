package clm.demo.unit.service;

import clm.demo.dto.requests.GenAppendixRequest;
import clm.demo.dto.responses.AppendixResponseDTO;
import clm.demo.exceptions.exceptions.InvalidAppendixStateException;
import clm.demo.exceptions.exceptions.ResourceNotFoundException;
import clm.demo.exceptions.exceptions.TemplateIncompleteException;
import clm.demo.mappers.AppendixMapper;
import clm.demo.models.Appendix;
import clm.demo.models.Contract;
import clm.demo.models.DocumentFieldValue;
import clm.demo.models.DocumentTemplate;
import clm.demo.models.enums.AppendixStatus;
import clm.demo.models.enums.ContractStatus;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.repositories.AppendixRepository;
import clm.demo.repositories.ContractRepository;
import clm.demo.repositories.DocumentFieldValueRepository;
import clm.demo.repositories.DocumentTemplateRepository;
import clm.demo.services.AppendixService;
import clm.demo.services.utility.DocumentGenerationUtil;
import clm.demo.utils.file.FileUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppendixServiceTest {

    @Mock AppendixRepository           appendixRepository;
    @Mock ContractRepository           contractRepository;
    @Mock DocumentTemplateRepository   templateRepository;
    @Mock DocumentFieldValueRepository fieldValueRepository;
    @Mock AppendixMapper               appendixMapper;
    @Mock FileUtils                    fileUtils;
    @Mock DocumentGenerationUtil       documentGenerationUtil;

    @InjectMocks AppendixService service;

    // ── getAppendicesForContract ──────────────────────────────────────────────

    @Nested
    class GetAppendicesForContract {

        @Test
        void should_return_appendices_for_existing_contract() {
            when(contractRepository.existsById(1L)).thenReturn(true);
            Appendix appendix = appendix(1L, AppendixStatus.DRAFT);
            AppendixResponseDTO dto = new AppendixResponseDTO();
            when(appendixRepository.findByContractId(1L)).thenReturn(List.of(appendix));
            when(appendixMapper.toResponseDTO(appendix)).thenReturn(dto);

            List<AppendixResponseDTO> result = service.getAppendicesForContract(1L);

            assertThat(result).hasSize(1).containsExactly(dto);
        }

        @Test
        void should_throw_when_contract_not_found() {
            when(contractRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> service.getAppendicesForContract(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        void should_return_empty_list_when_contract_has_no_appendices() {
            when(contractRepository.existsById(1L)).thenReturn(true);
            when(appendixRepository.findByContractId(1L)).thenReturn(List.of());

            assertThat(service.getAppendicesForContract(1L)).isEmpty();
        }
    }

    // ── uploadSignedAppendix ──────────────────────────────────────────────────

    @Nested
    class UploadSignedAppendix {

        @Test
        void should_throw_when_appendix_not_found() {
            when(appendixRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.uploadSignedAppendix(99L, new byte[]{1}, 1))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        void should_throw_when_appendix_already_signed() {
            Appendix signed = appendix(1L, AppendixStatus.SIGNED);
            when(appendixRepository.findById(1L)).thenReturn(Optional.of(signed));

            assertThatThrownBy(() -> service.uploadSignedAppendix(1L, new byte[]{1}, 1))
                    .isInstanceOf(InvalidAppendixStateException.class)
                    .hasMessageContaining("already SIGNED");
        }

        @Test
        void should_transition_draft_to_signed_when_valid_pdf_uploaded() throws IOException {
            Appendix draft = appendix(1L, AppendixStatus.DRAFT);
            AppendixResponseDTO dto = new AppendixResponseDTO();
            dto.setAppendixStatus("SIGNED");

            when(appendixRepository.findById(1L)).thenReturn(Optional.of(draft));
            when(fileUtils.compress(any())).thenReturn(new byte[]{9});
            when(appendixRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(appendixMapper.toResponseDTO(any())).thenReturn(dto);

            // PDF magic bytes: %PDF
            byte[] pdfBytes = new byte[]{0x25, 0x50, 0x44, 0x46, 0x2D, 1, 2, 3};
            AppendixResponseDTO result = service.uploadSignedAppendix(1L, pdfBytes, 42);

            assertThat(draft.getAppendixStatus()).isEqualTo(AppendixStatus.SIGNED);
            assertThat(draft.getUploadedSignedByUser()).isEqualTo(42);
            assertThat(draft.getUploadedSignedAt()).isNotNull();
        }
    }

    // ── terminateAppendix ─────────────────────────────────────────────────────

    @Nested
    class TerminateAppendix {

        @Test
        void should_terminate_signed_appendix() {
            Appendix signed = appendix(1L, AppendixStatus.SIGNED);
            AppendixResponseDTO dto = new AppendixResponseDTO();
            dto.setAppendixStatus("TERMINATED");

            when(appendixRepository.findById(1L)).thenReturn(Optional.of(signed));
            when(appendixRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(appendixMapper.toResponseDTO(any())).thenReturn(dto);

            AppendixResponseDTO result = service.terminateAppendix(1L);

            assertThat(signed.getAppendixStatus()).isEqualTo(AppendixStatus.TERMINATED);
        }

        @Test
        void should_throw_when_appendix_is_draft() {
            Appendix draft = appendix(1L, AppendixStatus.DRAFT);
            when(appendixRepository.findById(1L)).thenReturn(Optional.of(draft));

            assertThatThrownBy(() -> service.terminateAppendix(1L))
                    .isInstanceOf(InvalidAppendixStateException.class)
                    .hasMessageContaining("only SIGNED appendices");
        }

        @Test
        void should_throw_when_appendix_not_found() {
            when(appendixRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.terminateAppendix(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void should_throw_when_appendix_already_terminated() {
            Appendix terminated = appendix(1L, AppendixStatus.TERMINATED);
            when(appendixRepository.findById(1L)).thenReturn(Optional.of(terminated));

            assertThatThrownBy(() -> service.terminateAppendix(1L))
                    .isInstanceOf(InvalidAppendixStateException.class);
        }
    }

    // ── deleteAppendix ────────────────────────────────────────────────────────

    @Nested
    class DeleteAppendix {

        @Test
        void should_delete_appendix_when_it_exists() {
            when(appendixRepository.existsById(1L)).thenReturn(true);

            service.deleteAppendix(1L);

            verify(appendixRepository).deleteById(1L);
        }

        @Test
        void should_throw_when_appendix_not_found() {
            when(appendixRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> service.deleteAppendix(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");

            verify(appendixRepository, never()).deleteById(anyLong());
        }
    }

    // ── generateAppendix ─────────────────────────────────────────────────────

    @Nested
    class GenerateAppendix {

        @Test
        void should_throw_when_contract_not_found() {
            when(contractRepository.findById(99L)).thenReturn(Optional.empty());

            GenAppendixRequest req = new GenAppendixRequest(99L, 1L, "Title", 1L, null, Map.of("k", "v"));

            assertThatThrownBy(() -> service.generateAppendix(req))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Contract not found");
        }

        @Test
        void should_throw_when_template_not_found() {
            Contract contract = contract(1L);
            when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
            when(templateRepository.findById(99L)).thenReturn(Optional.empty());

            GenAppendixRequest req = new GenAppendixRequest(1L, 99L, "Title", 1L, null, Map.of("k", "v"));

            assertThatThrownBy(() -> service.generateAppendix(req))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Template not found");
        }

        @Test
        void should_throw_when_template_not_fully_mapped() {
            Contract contract = contract(1L);
            DocumentTemplate template = incompleteTemplate(2L);

            when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
            when(templateRepository.findById(2L)).thenReturn(Optional.of(template));

            GenAppendixRequest req = new GenAppendixRequest(1L, 2L, "Title", 1L, null, Map.of("k", "v"));

            assertThatThrownBy(() -> service.generateAppendix(req))
                    .isInstanceOf(TemplateIncompleteException.class)
                    .hasMessageContaining("not fully mapped");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static Appendix appendix(Long id, AppendixStatus status) {
        Appendix a = Appendix.builder()
                .title("Test Appendix")
                .appendixStatus(status)
                .fieldValues(new ArrayList<>())
                .build();
        setId(a, id);
        return a;
    }

    private static Contract contract(Long id) {
        Contract c = Contract.builder()
                .clientId(1)
                .contractStatus(ContractStatus.ACTIVE)
                .build();
        setId(c, id);
        return c;
    }

    private static DocumentTemplate incompleteTemplate(Long id) {
        DocumentTemplate t = DocumentTemplate.builder()
                .templateName("Incomplete-" + id)
                .documentFormat(DocumentFormat.DOCX)
                .documentContent(new byte[]{1})
                .fieldCount(2)
                .isFullyMapped(false)
                .templateFields(new ArrayList<>())
                .build();
        setId(t, id);
        return t;
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

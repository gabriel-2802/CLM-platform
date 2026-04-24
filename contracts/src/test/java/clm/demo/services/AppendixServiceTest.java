package clm.demo.services;

import clm.demo.dto.requests.GenAppendixRequest;
import clm.demo.dto.responses.AppendixResponseDTO;
import clm.demo.exceptions.InvalidAppendixStateException;
import clm.demo.exceptions.ResourceNotFoundException;
import clm.demo.exceptions.TemplateIncompleteException;
import clm.demo.mappers.AppendixMapper;
import clm.demo.models.Appendix;
import clm.demo.models.Contract;
import clm.demo.models.DocumentTemplate;
import clm.demo.models.enums.AppendixStatus;
import clm.demo.models.enums.ContractStatus;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.repositories.AppendixRepository;
import clm.demo.repositories.ContractRepository;
import clm.demo.repositories.DocumentFieldValueRepository;
import clm.demo.repositories.DocumentTemplateRepository;
import clm.demo.support.TestDataFactory;
import clm.demo.utils.docx.DocxFiller;
import clm.demo.utils.file.FileUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppendixServiceTest {

    @Mock AppendixRepository          appendixRepository;
    @Mock ContractRepository          contractRepository;
    @Mock DocumentTemplateRepository  templateRepository;
    @Mock DocumentFieldValueRepository fieldValueRepository;
    @Mock AppendixMapper              appendixMapper;
    @Mock FileUtils                   fileUtils;
    @Mock DocumentGenerationUtil      documentGenerationUtil;

    @InjectMocks AppendixService service;

    // ================================================================== //
    //  generateAppendix                                                    //
    // ================================================================== //

    @Nested
    class GenerateAppendix {

        @Test
        void contract_not_found_throws_resource_not_found() {
            when(contractRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.generateAppendix(TestDataFactory.genAppendixRequest()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Contract not found");
        }

        @Test
        void template_not_found_throws_resource_not_found() {
            Contract contract = TestDataFactory.contract(1L, ContractStatus.ACTIVE);
            when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
            when(templateRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.generateAppendix(TestDataFactory.genAppendixRequest()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Template not found");
        }

        @Test
        void template_not_fully_mapped_throws_template_incomplete() {
            Contract contract = TestDataFactory.contract(1L, ContractStatus.ACTIVE);
            DocumentTemplate template = TestDataFactory.templateWithId(1L, "NDA");
            template.setIsFullyMapped(false);

            when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
            when(templateRepository.findById(1L)).thenReturn(Optional.of(template));

            assertThatThrownBy(() -> service.generateAppendix(TestDataFactory.genAppendixRequest()))
                    .isInstanceOf(TemplateIncompleteException.class);
        }

        @Test
        void successful_generation_creates_draft_appendix() throws IOException {
            Contract contract   = TestDataFactory.contract(1L, ContractStatus.ACTIVE);
            DocumentTemplate t  = TestDataFactory.templateWithId(1L, "NDA");
            Appendix appendix   = TestDataFactory.appendix(12L, contract, AppendixStatus.DRAFT);
            AppendixResponseDTO dto = TestDataFactory.appendixResponse(12L, "DRAFT");

            when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
            when(templateRepository.findById(1L)).thenReturn(Optional.of(t));
            when(documentGenerationUtil.buildFieldValues(any(), any(), any())).thenReturn(List.of());
            when(documentGenerationUtil.buildLabelValueMap(any())).thenReturn(java.util.Map.of());
            when(appendixRepository.save(any())).thenReturn(appendix);
            when(fileUtils.decompress(any())).thenReturn(new byte[]{1, 2, 3});
            when(fileUtils.convert(any(), eq(DocumentFormat.DOCX), eq(DocumentFormat.PDF)))
                    .thenReturn(new byte[]{4, 5});
            when(fileUtils.compress(any())).thenReturn(new byte[]{6});
            when(appendixMapper.toResponseDTO(any())).thenReturn(dto);

            // DocxFiller.fillDocx is a static method on a @UtilityClass — mock it statically
            try (MockedStatic<DocxFiller> docxFiller = mockStatic(DocxFiller.class)) {
                docxFiller.when(() -> DocxFiller.fillDocx(any(), any(), any()))
                        .thenReturn(new byte[]{1, 2, 3});

                AppendixResponseDTO result = service.generateAppendix(TestDataFactory.genAppendixRequest());

                assertThat(result.getAppendixStatus()).isEqualTo("DRAFT");
                verify(appendixRepository, atLeastOnce()).save(any());
            }
        }
    }

    // ================================================================== //
    //  uploadDirectAppendix                                                //
    // ================================================================== //

    @Nested
    class UploadDirectAppendix {

        @Test
        void contract_not_found_throws_resource_not_found() {
            when(contractRepository.findById(1L)).thenReturn(Optional.empty());

            var request = mockDirectUploadRequest(1L, TestDataFactory.pdfMagicBytes());

            assertThatThrownBy(() -> service.uploadDirectAppendix(request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void successful_upload_sets_status_to_signed() throws IOException {
            Contract contract = TestDataFactory.contract(1L, ContractStatus.ACTIVE);
            Appendix appendix = TestDataFactory.appendix(13L, contract, AppendixStatus.SIGNED);
            AppendixResponseDTO dto = TestDataFactory.appendixResponse(13L, "SIGNED");

            when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
            when(fileUtils.compress(any())).thenReturn(new byte[]{1});
            when(appendixRepository.save(any())).thenReturn(appendix);
            when(appendixMapper.toResponseDTO(any())).thenReturn(dto);

            var request = mockDirectUploadRequest(1L, TestDataFactory.pdfMagicBytes());
            AppendixResponseDTO result = service.uploadDirectAppendix(request);

            assertThat(result.getAppendixStatus()).isEqualTo("SIGNED");
        }
    }

    // ================================================================== //
    //  uploadSignedAppendix                                                //
    // ================================================================== //

    @Nested
    class UploadSignedAppendix {

        @Test
        void appendix_not_found_throws_resource_not_found() {
            when(appendixRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.uploadSignedAppendix(99L, TestDataFactory.pdfMagicBytes()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void already_signed_appendix_throws_invalid_state() {
            Contract contract = TestDataFactory.contract(1L, ContractStatus.ACTIVE);
            Appendix appendix = TestDataFactory.appendix(12L, contract, AppendixStatus.SIGNED);

            when(appendixRepository.findById(12L)).thenReturn(Optional.of(appendix));

            assertThatThrownBy(() -> service.uploadSignedAppendix(12L, TestDataFactory.pdfMagicBytes()))
                    .isInstanceOf(InvalidAppendixStateException.class)
                    .hasMessageContaining("already SIGNED");
        }

        @Test
        void draft_appendix_transitions_to_signed() throws IOException {
            Contract contract = TestDataFactory.contract(1L, ContractStatus.ACTIVE);
            Appendix appendix = TestDataFactory.appendix(12L, contract, AppendixStatus.DRAFT);
            AppendixResponseDTO dto = TestDataFactory.appendixResponse(12L, "SIGNED");

            when(appendixRepository.findById(12L)).thenReturn(Optional.of(appendix));
            when(fileUtils.compress(any())).thenReturn(new byte[]{1});
            when(appendixRepository.save(any())).thenReturn(appendix);
            when(appendixMapper.toResponseDTO(any())).thenReturn(dto);

            AppendixResponseDTO result = service.uploadSignedAppendix(12L, TestDataFactory.pdfMagicBytes());

            assertThat(result.getAppendixStatus()).isEqualTo("SIGNED");
            assertThat(appendix.getAppendixStatus()).isEqualTo(AppendixStatus.SIGNED);
        }

        @Test
        void docx_upload_is_converted_to_pdf() throws IOException {
            Contract contract = TestDataFactory.contract(1L, ContractStatus.ACTIVE);
            Appendix appendix = TestDataFactory.appendix(12L, contract, AppendixStatus.DRAFT);
            AppendixResponseDTO dto = TestDataFactory.appendixResponse(12L, "SIGNED");

            when(appendixRepository.findById(12L)).thenReturn(Optional.of(appendix));
            when(fileUtils.convert(any(), eq(DocumentFormat.DOCX), eq(DocumentFormat.PDF)))
                    .thenReturn(TestDataFactory.pdfMagicBytes());
            when(fileUtils.compress(any())).thenReturn(new byte[]{1});
            when(appendixRepository.save(any())).thenReturn(appendix);
            when(appendixMapper.toResponseDTO(any())).thenReturn(dto);

            service.uploadSignedAppendix(12L, TestDataFactory.docxMagicBytes());

            verify(fileUtils).convert(any(), eq(DocumentFormat.DOCX), eq(DocumentFormat.PDF));
        }
    }

    // ================================================================== //
    //  getAppendicesForContract                                            //
    // ================================================================== //

    @Nested
    class GetAppendicesForContract {

        @Test
        void contract_not_found_throws_resource_not_found() {
            when(contractRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> service.getAppendicesForContract(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void returns_mapped_list_for_existing_contract() {
            Contract contract = TestDataFactory.contract(1L, ContractStatus.ACTIVE);
            Appendix appendix = TestDataFactory.appendix(12L, contract, AppendixStatus.DRAFT);
            AppendixResponseDTO dto = TestDataFactory.appendixResponse(12L, "DRAFT");

            when(contractRepository.existsById(1L)).thenReturn(true);
            when(appendixRepository.findByContractId(1L)).thenReturn(List.of(appendix));
            when(appendixMapper.toResponseDTO(appendix)).thenReturn(dto);

            List<AppendixResponseDTO> result = service.getAppendicesForContract(1L);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getId()).isEqualTo(12L);
        }

        @Test
        void empty_contract_returns_empty_list() {
            when(contractRepository.existsById(1L)).thenReturn(true);
            when(appendixRepository.findByContractId(1L)).thenReturn(List.of());

            assertThat(service.getAppendicesForContract(1L)).isEmpty();
        }
    }

    // ================================================================== //
    //  deleteAppendix                                                      //
    // ================================================================== //

    @Nested
    class DeleteAppendix {

        @Test
        void existing_appendix_is_deleted() {
            when(appendixRepository.existsById(12L)).thenReturn(true);

            service.deleteAppendix(12L);

            verify(appendixRepository).deleteById(12L);
        }

        @Test
        void non_existent_appendix_throws_resource_not_found() {
            when(appendixRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> service.deleteAppendix(99L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(appendixRepository, never()).deleteById(any());
        }
    }

    // ------------------------------------------------------------------ //
    //  helpers                                                             //
    // ------------------------------------------------------------------ //

    private clm.demo.dto.requests.UploadDirectAppendixRequest mockDirectUploadRequest(
            Long contractId, byte[] fileBytes) {

        org.springframework.mock.web.MockMultipartFile file =
                new org.springframework.mock.web.MockMultipartFile(
                        "file", "test.pdf", "application/pdf", fileBytes);

        clm.demo.dto.requests.UploadDirectAppendixRequest req =
                new clm.demo.dto.requests.UploadDirectAppendixRequest();
        req.setContractId(contractId);
        req.setTitle("Exhibit A");
        req.setFile(file);
        return req;
    }
}

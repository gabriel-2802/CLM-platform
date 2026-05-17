package clm.demo.services;

import clm.demo.dto.requests.ContractTerminationRequest;
import clm.demo.dto.requests.GenContractRequest;
import clm.demo.dto.requests.RenegotiateContractRequest;
import clm.demo.dto.requests.SearchRequest;
import clm.demo.dto.responses.ContractResponseDTO;
import clm.demo.exceptions.exceptions.InvalidContractStateException;
import clm.demo.exceptions.exceptions.ResourceNotFoundException;
import clm.demo.exceptions.exceptions.TemplateIncompleteException;
import clm.demo.mappers.ContractGenerationMapper;
import clm.demo.mappers.GeneratedContractMapper;
import clm.demo.models.Contract;
import clm.demo.models.DocumentTemplate;
import clm.demo.models.enums.ContractStatus;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.repositories.ContractRepository;
import clm.demo.repositories.DocumentFieldValueRepository;
import clm.demo.repositories.DocumentTemplateRepository;
import clm.demo.services.utility.DocumentGenerationUtil;
import clm.demo.specifications.ContractSpecification;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractServiceTest {

    @Mock DocumentTemplateRepository    templateRepository;
    @Mock ContractRepository            contractRepository;
    @Mock DocumentFieldValueRepository  fieldValueRepository;
    @Mock ContractGenerationMapper      generationMapper;
    @Mock GeneratedContractMapper       contractMapper;
    @Mock ContractSpecification         contractSpecification;
    @Mock FileUtils                     fileUtils;
    @Mock
    DocumentGenerationUtil documentGenerationUtil;

    @InjectMocks ContractService service;

    // ================================================================== //
    //  generateContract                                                    //
    // ================================================================== //

    @Nested
    class GenerateContract {

        @Test
        void template_not_found_throws_resource_not_found_exception() {
            when(templateRepository.findById(1L)).thenReturn(Optional.empty());

            GenContractRequest req = TestDataFactory.genContractRequest();

            assertThatThrownBy(() -> service.generateContract(req))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("1");
        }

        @Test
        void template_not_fully_mapped_throws_template_incomplete() {
            DocumentTemplate template = TestDataFactory.templateWithId(1L, "NDA");
            template.setIsFullyMapped(false);

            when(templateRepository.findById(1L)).thenReturn(Optional.of(template));

            assertThatThrownBy(() -> service.generateContract(TestDataFactory.genContractRequest()))
                    .isInstanceOf(TemplateIncompleteException.class);
        }

        @Test
        void generation_success_saves_contract_and_returns_dto() throws IOException {
            DocumentTemplate template = TestDataFactory.templateWithId(1L, "NDA");
            Contract contract = TestDataFactory.contract(88L, ContractStatus.PENDING_SIGNATURE);
            ContractResponseDTO dto = TestDataFactory.contractResponse(88L, "PENDING_SIGNATURE");

            when(templateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(generationMapper.toContractEntity(any(), any())).thenReturn(contract);
            when(contractRepository.save(any())).thenReturn(contract);
            when(documentGenerationUtil.buildFieldValues(any(), any(), any())).thenReturn(List.of());
            when(documentGenerationUtil.buildLabelValueMap(any())).thenReturn(java.util.Map.of());
            when(fileUtils.decompress(any())).thenReturn(new byte[]{1, 2, 3});
            when(fileUtils.convert(any(), eq(DocumentFormat.DOCX), eq(DocumentFormat.PDF))).thenReturn(new byte[]{4, 5, 6});
            when(fileUtils.compress(any())).thenReturn(new byte[]{7, 8, 9});
            when(contractMapper.toResponseDTO(any())).thenReturn(dto);

            // DocxFiller.fillDocx is a static method on a @UtilityClass — mock it statically
            try (MockedStatic<DocxFiller> docxFiller = mockStatic(DocxFiller.class)) {
                docxFiller.when(() -> DocxFiller.fillDocx(any(), any(), any()))
                        .thenReturn(new byte[]{1, 2, 3});

                ContractResponseDTO result = service.generateContract(TestDataFactory.genContractRequest());

                assertThat(result.getId()).isEqualTo(88L);
                verify(contractRepository, atLeastOnce()).save(any());
            }
        }
    }

    // ================================================================== //
    //  uploadSignedContract                                                //
    // ================================================================== //

    @Nested
    class UploadSignedContract {

        @Test
        void contract_not_found_throws_resource_not_found_exception() {
            when(contractRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.uploadSignedContract(99L, TestDataFactory.pdfMagicBytes(), 42))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void pdf_upload_accepted_and_contract_set_active() throws IOException {
            Contract contract = TestDataFactory.contract(1L, ContractStatus.PENDING_SIGNATURE);
            ContractResponseDTO dto = TestDataFactory.contractResponse(1L, "ACTIVE");

            when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
            when(fileUtils.compress(any())).thenReturn(new byte[]{1});
            when(contractRepository.save(any())).thenReturn(contract);
            when(contractMapper.toResponseDTO(any())).thenReturn(dto);

            ContractResponseDTO result = service.uploadSignedContract(1L, TestDataFactory.pdfMagicBytes(), 42);

            assertThat(result.getContractStatus()).isEqualTo("ACTIVE");
            assertThat(contract.getContractStatus()).isEqualTo(ContractStatus.ACTIVE);
        }

        @Test
        void docx_upload_converted_to_pdf_before_saving() throws IOException {
            Contract contract = TestDataFactory.contract(1L, ContractStatus.PENDING_SIGNATURE);
            ContractResponseDTO dto = TestDataFactory.contractResponse(1L, "ACTIVE");

            when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
            when(fileUtils.convert(any(), eq(DocumentFormat.DOCX), eq(DocumentFormat.PDF)))
                    .thenReturn(TestDataFactory.pdfMagicBytes());
            when(fileUtils.compress(any())).thenReturn(new byte[]{1});
            when(contractRepository.save(any())).thenReturn(contract);
            when(contractMapper.toResponseDTO(any())).thenReturn(dto);

            service.uploadSignedContract(1L, TestDataFactory.docxMagicBytes(), 42);

            verify(fileUtils).convert(any(), eq(DocumentFormat.DOCX), eq(DocumentFormat.PDF));
        }
    }

    // ================================================================== //
    //  terminateContract                                                   //
    // ================================================================== //

    @Nested
    class TerminateContract {

        @Test
        void active_contract_terminated_successfully() {
            Contract contract = TestDataFactory.contract(1L, ContractStatus.ACTIVE);
            ContractTerminationRequest req = TestDataFactory.terminationRequest();

            when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
            when(contractRepository.save(any())).thenReturn(contract);

            service.terminateContract(1L, req);

            assertThat(contract.getContractStatus()).isEqualTo(ContractStatus.TERMINATED);
            assertThat(contract.getTerminationDate()).isEqualTo(req.getTerminationDate());
            assertThat(contract.getReasonsForTermination()).isEqualTo("early exit");
        }

        @Test
        void contract_not_found_throws_resource_not_found_exception() {
            when(contractRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.terminateContract(99L, TestDataFactory.terminationRequest()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void pending_contract_cannot_be_terminated() {
            Contract contract = TestDataFactory.contract(1L, ContractStatus.PENDING_SIGNATURE);
            when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));

            assertThatThrownBy(() -> service.terminateContract(1L, TestDataFactory.terminationRequest()))
                    .isInstanceOf(InvalidContractStateException.class)
                    .hasMessageContaining("PENDING_SIGNATURE");
        }

        @Test
        void already_terminated_contract_cannot_be_terminated_again() {
            Contract contract = TestDataFactory.contract(1L, ContractStatus.TERMINATED);
            when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));

            assertThatThrownBy(() -> service.terminateContract(1L, TestDataFactory.terminationRequest()))
                    .isInstanceOf(InvalidContractStateException.class);
        }

        @Test
        void archived_contract_cannot_be_terminated() {
            Contract contract = TestDataFactory.contract(1L, ContractStatus.ARCHIVED);
            when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));

            assertThatThrownBy(() -> service.terminateContract(1L, TestDataFactory.terminationRequest()))
                    .isInstanceOf(InvalidContractStateException.class);
        }
    }

    // ================================================================== //
    //  getAll                                                              //
    // ================================================================== //

    @Nested
    class GetAll {

        @Test
        void returns_mapped_page_of_contracts() {
            Contract c = TestDataFactory.contract(1L, ContractStatus.ACTIVE);
            ContractResponseDTO dto = TestDataFactory.contractResponse(1L, "ACTIVE");

            when(contractRepository.findAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(c)));
            when(contractMapper.toResponseDTO(c)).thenReturn(dto);

            var page = service.getAll(0, 20);

            assertThat(page.getContent()).hasSize(1);
        }
    }

    // ================================================================== //
    //  search                                                              //
    // ================================================================== //

    @Nested
    class Search {

        @Test
        void delegates_to_specification_and_repository() {
            SearchRequest req = TestDataFactory.searchRequest();
            Contract c = TestDataFactory.contract(1L, ContractStatus.ACTIVE);
            ContractResponseDTO dto = TestDataFactory.contractResponse(1L, "ACTIVE");

            when(contractSpecification.buildSearchSpecification(req))
                    .thenReturn((root, query, cb) -> null);
            when(contractRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(c)));
            when(contractMapper.toResponseDTO(c)).thenReturn(dto);

            var page = service.search(req);

            assertThat(page.getContent()).hasSize(1);
            verify(contractSpecification).buildSearchSpecification(req);
        }

        @Test
        void null_page_and_size_use_defaults() {
            SearchRequest req = new SearchRequest(null, null, null, null, null, null, null, null, null, null, null);

            when(contractSpecification.buildSearchSpecification(req))
                    .thenReturn((root, query, cb) -> null);
            when(contractRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            service.search(req); // no exception; defaults applied internally
        }
    }

    // ================================================================== //
    //  renegotiateContract                                                  //
    // ================================================================== //

    @Nested
    class RenegotiateContract {

        @Test
        void updates_value_and_end_date_for_active_contract() {
            Contract contract = TestDataFactory.contract(1L, ContractStatus.ACTIVE);
            RenegotiateContractRequest req = new RenegotiateContractRequest(
                    BigDecimal.valueOf(15_000), LocalDate.of(2028, 6, 1));

            when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
            when(contractRepository.save(any())).thenReturn(contract);

            service.renegotiateContract(1L, req);

            assertThat(contract.getContractValue()).isEqualByComparingTo(BigDecimal.valueOf(15_000));
            assertThat(contract.getContractEndDate()).isEqualTo(LocalDate.of(2028, 6, 1));
        }

        @Test
        void updates_only_value_when_end_date_is_null() {
            Contract contract = TestDataFactory.contract(1L, ContractStatus.ACTIVE);
            LocalDate originalEndDate = contract.getContractEndDate();
            RenegotiateContractRequest req = new RenegotiateContractRequest(
                    BigDecimal.valueOf(20_000), null);

            when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
            when(contractRepository.save(any())).thenReturn(contract);

            service.renegotiateContract(1L, req);

            assertThat(contract.getContractValue()).isEqualByComparingTo(BigDecimal.valueOf(20_000));
            assertThat(contract.getContractEndDate()).isEqualTo(originalEndDate);
        }

        @Test
        void updates_only_end_date_when_value_is_null() {
            Contract contract = TestDataFactory.contract(1L, ContractStatus.ACTIVE);
            BigDecimal originalValue = contract.getContractValue();
            RenegotiateContractRequest req = new RenegotiateContractRequest(
                    null, LocalDate.of(2029, 1, 1));

            when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
            when(contractRepository.save(any())).thenReturn(contract);

            service.renegotiateContract(1L, req);

            assertThat(contract.getContractValue()).isEqualByComparingTo(originalValue);
            assertThat(contract.getContractEndDate()).isEqualTo(LocalDate.of(2029, 1, 1));
        }

        @Test
        void saves_contract_after_update() {
            Contract contract = TestDataFactory.contract(1L, ContractStatus.ACTIVE);
            RenegotiateContractRequest req = new RenegotiateContractRequest(
                    BigDecimal.valueOf(15_000), LocalDate.of(2028, 6, 1));

            when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
            when(contractRepository.save(any())).thenReturn(contract);

            service.renegotiateContract(1L, req);

            verify(contractRepository).save(contract);
        }

        @Test
        void throws_resource_not_found_when_contract_missing() {
            when(contractRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.renegotiateContract(99L,
                    new RenegotiateContractRequest(BigDecimal.valueOf(5_000), null)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        void throws_invalid_state_when_contract_is_not_active() {
            Contract contract = TestDataFactory.contract(1L, ContractStatus.TERMINATED);
            when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));

            assertThatThrownBy(() -> service.renegotiateContract(1L,
                    new RenegotiateContractRequest(BigDecimal.valueOf(5_000), null)))
                    .isInstanceOf(InvalidContractStateException.class)
                    .hasMessageContaining("TERMINATED");
        }

        @Test
        void throws_invalid_state_when_contract_is_pending_signature() {
            Contract contract = TestDataFactory.contract(1L, ContractStatus.PENDING_SIGNATURE);
            when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));

            assertThatThrownBy(() -> service.renegotiateContract(1L,
                    new RenegotiateContractRequest(null, LocalDate.of(2028, 1, 1))))
                    .isInstanceOf(InvalidContractStateException.class)
                    .hasMessageContaining("PENDING_SIGNATURE");
        }
    }
}

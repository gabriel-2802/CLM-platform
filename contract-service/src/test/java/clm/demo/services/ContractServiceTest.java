package clm.demo.services;

import clm.demo.dto.requests.ContractTerminationRequest;
import clm.demo.dto.requests.GenContractRequest;
import clm.demo.dto.requests.SearchRequest;
import clm.demo.dto.responses.ContractResponseDTO;
import clm.demo.exceptions.InvalidContractStateException;
import clm.demo.exceptions.ResourceNotFoundException;
import clm.demo.exceptions.TemplateIncompleteException;
import clm.demo.mappers.ContractGenerationMapper;
import clm.demo.mappers.GeneratedContractMapper;
import clm.demo.models.Contract;
import clm.demo.models.DocumentTemplate;
import clm.demo.models.enums.ContractStatus;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.repositories.ContractRepository;
import clm.demo.repositories.DocumentFieldValueRepository;
import clm.demo.repositories.DocumentTemplateRepository;
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
    @Mock DocumentGenerationUtil        documentGenerationUtil;

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

            assertThatThrownBy(() -> service.uploadSignedContract(99L, TestDataFactory.pdfMagicBytes()))
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

            ContractResponseDTO result = service.uploadSignedContract(1L, TestDataFactory.pdfMagicBytes());

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

            service.uploadSignedContract(1L, TestDataFactory.docxMagicBytes());

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
}

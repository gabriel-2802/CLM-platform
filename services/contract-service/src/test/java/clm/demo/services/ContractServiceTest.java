package clm.demo.services;

import clm.demo.dto.requests.ContractUpdateRequest;
import clm.demo.dto.requests.RenegotiateContractRequest;
import clm.demo.dto.responses.ContractResponseDTO;
import clm.demo.exceptions.exceptions.InvalidContractStateException;
import clm.demo.exceptions.exceptions.InvalidContractUpdateException;
import clm.demo.exceptions.exceptions.ResourceNotFoundException;
import clm.demo.mappers.ContractGenerationMapper;
import clm.demo.mappers.GeneratedContractMapper;
import clm.demo.models.Appendix;
import clm.demo.models.Contract;
import clm.demo.models.ContractDetails;
import clm.demo.models.enums.ContractStatus;
import clm.demo.repositories.AppendixRepository;
import clm.demo.repositories.ContractDetailsRepository;
import clm.demo.repositories.ContractRepository;
import clm.demo.repositories.DocumentFieldValueRepository;
import clm.demo.repositories.DocumentTemplateRepository;
import clm.demo.services.utility.DocumentGenerationUtil;
import clm.demo.specifications.ContractSpecification;
import clm.demo.support.TestDataFactory;
import clm.demo.utils.file.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractServiceTest {

    @Mock DocumentTemplateRepository    templateRepository;
    @Mock ContractRepository            contractRepository;
    @Mock ContractDetailsRepository     contractDetailsRepository;
    @Mock AppendixRepository            appendixRepository;
    @Mock DocumentFieldValueRepository  fieldValueRepository;
    @Mock ContractGenerationMapper      generationMapper;
    @Mock GeneratedContractMapper       contractMapper;
    @Mock ContractSpecification         contractSpecification;
    @Mock FileUtils                     fileUtils;
    @Mock DocumentGenerationUtil        documentGenerationUtil;

    @InjectMocks ContractService service;

    @Nested
    class UpdateContractTerms {

        private Contract contract;

        @BeforeEach
        void setUp() {
            contract = TestDataFactory.contract(1L, ContractStatus.ACTIVE);
            when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
            when(contractDetailsRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(contractMapper.toResponseDTO(any(), any())).thenReturn(new ContractResponseDTO());
        }

        private Appendix appendixWithEffectiveDate(Long id, LocalDate effectiveDate) {
            Appendix a = mock(Appendix.class);
            when(a.getId()).thenReturn(id);
            when(a.getEffectiveDate()).thenReturn(effectiveDate);
            when(appendixRepository.findById(id)).thenReturn(Optional.of(a));
            return a;
        }

        @Test
        void should_skip_date_validation_when_appendix_effective_date_is_null() {
            appendixWithEffectiveDate(1L, null);
            ContractUpdateRequest req = new ContractUpdateRequest(
                    1, 1, LocalDate.of(2026, 6, 1), LocalDate.of(2027, 1, 1), null, BigDecimal.valueOf(15_000));

            // should not throw — null effectiveDate means no validation
            ContractResponseDTO result = service.updateContractTerms(1L, req);
            assertThat(result).isNotNull();
        }

        @Test
        void should_succeed_when_appendix_effective_date_matches_start_date() {
            LocalDate date = LocalDate.of(2026, 6, 1);
            appendixWithEffectiveDate(1L, date);
            ContractUpdateRequest req = new ContractUpdateRequest(
                    1, 1, date, LocalDate.of(2027, 1, 1), null, BigDecimal.valueOf(15_000));

            ContractResponseDTO result = service.updateContractTerms(1L, req);
            assertThat(result).isNotNull();
        }

        @Test
        void should_throw_when_appendix_effective_date_does_not_match_start_date() {
            appendixWithEffectiveDate(1L, LocalDate.of(2026, 5, 1));
            ContractUpdateRequest req = new ContractUpdateRequest(
                    1, 1, LocalDate.of(2026, 6, 1), LocalDate.of(2027, 1, 1), null, BigDecimal.valueOf(15_000));

            assertThatThrownBy(() -> service.updateContractTerms(1L, req))
                    .isInstanceOf(InvalidContractUpdateException.class)
                    .hasMessageContaining("must match");
        }

        @Test
        void should_throw_when_contract_not_active() {
            Contract pending = TestDataFactory.contract(2L, ContractStatus.PENDING_SIGNATURE);
            when(contractRepository.findById(2L)).thenReturn(Optional.of(pending));
            ContractUpdateRequest req = new ContractUpdateRequest(
                    1, 1, LocalDate.of(2026, 6, 1), null, null, BigDecimal.valueOf(5_000));

            assertThatThrownBy(() -> service.updateContractTerms(2L, req))
                    .isInstanceOf(InvalidContractStateException.class)
                    .hasMessageContaining("PENDING_SIGNATURE");
        }

        @Test
        void should_throw_when_no_fields_provided() {
            ContractUpdateRequest req = new ContractUpdateRequest(1, 1, LocalDate.of(2026, 6, 1), null, null, null);

            assertThatThrownBy(() -> service.updateContractTerms(1L, req))
                    .isInstanceOf(InvalidContractUpdateException.class)
                    .hasMessageContaining("At least one");
        }
    }

    @Nested
    class RenegotiateContract {

        private Contract contract;
        private Appendix appendix;

        @BeforeEach
        void setUp() {
            contract = TestDataFactory.contract(1L, ContractStatus.ACTIVE);
            appendix = mock(Appendix.class);
            when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
            when(appendixRepository.findById(1L)).thenReturn(Optional.of(appendix));
            when(contractDetailsRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        }

//        @Test
//        void creates_new_details_with_provided_value_and_end_date() {
//            var req = new RenegotiateContractRequest(1, 1, BigDecimal.valueOf(15_000), LocalDate.of(2028, 6, 1));
//            service.renegotiateContract(1L, req);
//
//            var captor = ArgumentCaptor.forClass(ContractDetails.class);
//            verify(contractDetailsRepository).save(captor.capture());
//            assertThat(captor.getValue().getContractValue()).isEqualByComparingTo(BigDecimal.valueOf(15_000));
//            assertThat(captor.getValue().getEndDate()).isEqualTo(LocalDate.of(2028, 6, 1));
//        }
//
//        @Test
//        void falls_back_to_current_value_when_new_value_is_null() {
//            var req = new RenegotiateContractRequest(1, 1, null, LocalDate.of(2029, 1, 1));
//            service.renegotiateContract(1L, req);
//
//            var captor = ArgumentCaptor.forClass(ContractDetails.class);
//            verify(contractDetailsRepository).save(captor.capture());
//            assertThat(captor.getValue().getContractValue()).isNull();
//            assertThat(captor.getValue().getEndDate()).isEqualTo(LocalDate.of(2029, 1, 1));
//        }
//
//        @Test
//        void falls_back_to_current_end_date_when_new_end_date_is_null() {
//            var req = new RenegotiateContractRequest(1, 1, BigDecimal.valueOf(20_000), null);
//            service.renegotiateContract(1L, req);
//
//            var captor = ArgumentCaptor.forClass(ContractDetails.class);
//            verify(contractDetailsRepository).save(captor.capture());
//            assertThat(captor.getValue().getContractValue()).isEqualByComparingTo(BigDecimal.valueOf(20_000));
//            assertThat(captor.getValue().getEndDate()).isNull();
//        }
//
//        @Test
//        void saves_new_contract_details_record() {
//            var req = new RenegotiateContractRequest(1, 1, BigDecimal.valueOf(15_000), LocalDate.of(2028, 6, 1));
//            service.renegotiateContract(1L, req);
//            verify(contractDetailsRepository).save(any(ContractDetails.class));
//        }
//
//        @Test
//        void throws_resource_not_found_when_contract_missing() {
//            when(contractRepository.findById(99L)).thenReturn(Optional.empty());
//            assertThatThrownBy(() -> service.renegotiateContract(99L,
//                    new RenegotiateContractRequest(1, 1, BigDecimal.valueOf(5_000), null)))
//                    .isInstanceOf(ResourceNotFoundException.class)
//                    .hasMessageContaining("99");
//        }
//
//        @Test
//        void throws_invalid_state_when_contract_is_not_active() {
//            Contract terminated = TestDataFactory.contract(2L, ContractStatus.TERMINATED);
//            when(contractRepository.findById(2L)).thenReturn(Optional.of(terminated));
//            assertThatThrownBy(() -> service.renegotiateContract(2L,
//                    new RenegotiateContractRequest(1, 1, BigDecimal.valueOf(5_000), null)))
//                    .isInstanceOf(InvalidContractStateException.class)
//                    .hasMessageContaining("TERMINATED");
//        }
//
//        @Test
//        void throws_invalid_state_when_contract_is_pending_signature() {
//            Contract pending = TestDataFactory.contract(3L, ContractStatus.PENDING_SIGNATURE);
//            when(contractRepository.findById(3L)).thenReturn(Optional.of(pending));
//            assertThatThrownBy(() -> service.renegotiateContract(3L,
//                    new RenegotiateContractRequest(1, 1, null, LocalDate.of(2028, 1, 1))))
//                    .isInstanceOf(InvalidContractStateException.class)
//                    .hasMessageContaining("PENDING_SIGNATURE");
//        }
    }
}

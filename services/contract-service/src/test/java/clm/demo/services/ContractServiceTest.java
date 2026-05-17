package clm.demo.services;

import clm.demo.dto.requests.RenegotiateContractRequest;
import clm.demo.exceptions.exceptions.InvalidContractStateException;
import clm.demo.exceptions.exceptions.ResourceNotFoundException;
import clm.demo.mappers.ContractGenerationMapper;
import clm.demo.mappers.GeneratedContractMapper;
import clm.demo.models.Contract;
import clm.demo.models.enums.ContractStatus;
import clm.demo.repositories.ContractRepository;
import clm.demo.repositories.DocumentFieldValueRepository;
import clm.demo.repositories.DocumentTemplateRepository;
import clm.demo.services.utility.DocumentGenerationUtil;
import clm.demo.specifications.ContractSpecification;
import clm.demo.support.TestDataFactory;
import clm.demo.utils.file.FileUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @Mock DocumentTemplateRepository   templateRepository;
    @Mock ContractRepository           contractRepository;
    @Mock DocumentFieldValueRepository fieldValueRepository;
    @Mock ContractGenerationMapper     generationMapper;
    @Mock GeneratedContractMapper      contractMapper;
    @Mock ContractSpecification        contractSpecification;
    @Mock FileUtils                    fileUtils;
    @Mock DocumentGenerationUtil       documentGenerationUtil;

    @InjectMocks ContractService service;

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

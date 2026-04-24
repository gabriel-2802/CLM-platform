package clm.demo.services;

import clm.demo.dto.responses.ContractResponseDTO;
import clm.demo.mappers.GeneratedContractMapper;
import clm.demo.models.Contract;
import clm.demo.models.enums.ContractStatus;
import clm.demo.repositories.ContractRepository;
import clm.demo.support.TestDataFactory;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock ContractRepository     contractRepository;
    @Mock GeneratedContractMapper contractMapper;

    @InjectMocks ReportService service;

    // ================================================================== //
    //  getExpiringContracts                                                //
    // ================================================================== //

    @Nested
    class GetExpiringContracts {

        @Test
        void returns_mapped_contracts_from_repository() {
            Contract contract = TestDataFactory.contract(1L, ContractStatus.ACTIVE);
            ContractResponseDTO dto = TestDataFactory.contractResponse(1L, "ACTIVE");

            when(contractRepository.findExpiringContracts(eq(ContractStatus.ACTIVE), any(), any()))
                    .thenReturn(List.of(contract));
            when(contractMapper.toResponseDTO(contract)).thenReturn(dto);

            List<ContractResponseDTO> result = service.getExpiringContracts(30);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getId()).isEqualTo(1L);
        }

        @Test
        void empty_result_returns_empty_list() {
            when(contractRepository.findExpiringContracts(any(), any(), any()))
                    .thenReturn(List.of());

            assertThat(service.getExpiringContracts(30)).isEmpty();
        }

        @Test
        void deadline_is_today_plus_given_days() {
            // verify that the deadline passed to the repo is today + days
            when(contractRepository.findExpiringContracts(any(), any(), any()))
                    .thenReturn(List.of());

            service.getExpiringContracts(7);

            LocalDate today    = LocalDate.now();
            LocalDate deadline = today.plusDays(7);

            verify(contractRepository).findExpiringContracts(
                    eq(ContractStatus.ACTIVE),
                    eq(today),
                    eq(deadline));
        }
    }

    // ================================================================== //
    //  getInactiveClientContracts                                          //
    // ================================================================== //

    @Nested
    class GetInactiveClientContracts {

        @Test
        void returns_mapped_contracts_from_repository() {
            Contract contract = TestDataFactory.contract(2L, ContractStatus.ACTIVE);
            ContractResponseDTO dto = TestDataFactory.contractResponse(2L, "ACTIVE");

            when(contractRepository.findInactiveClientContracts(eq(ContractStatus.ACTIVE), any()))
                    .thenReturn(List.of(contract));
            when(contractMapper.toResponseDTO(contract)).thenReturn(dto);

            List<ContractResponseDTO> result = service.getInactiveClientContracts(6);

            assertThat(result).hasSize(1);
        }

        @Test
        void empty_result_returns_empty_list() {
            when(contractRepository.findInactiveClientContracts(any(), any()))
                    .thenReturn(List.of());

            assertThat(service.getInactiveClientContracts(6)).isEmpty();
        }

        @Test
        void cutoff_date_is_today_minus_given_months() {
            when(contractRepository.findInactiveClientContracts(any(), any()))
                    .thenReturn(List.of());

            service.getInactiveClientContracts(3);

            LocalDate cutoff = LocalDate.now().minusMonths(3);

            verify(contractRepository).findInactiveClientContracts(
                    eq(ContractStatus.ACTIVE),
                    eq(cutoff));
        }
    }
}

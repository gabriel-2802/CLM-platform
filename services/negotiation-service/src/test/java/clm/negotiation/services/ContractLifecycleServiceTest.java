package clm.negotiation.services;

import clm.negotiation.dto.requests.ContractActivatedRequest;
import clm.negotiation.dto.requests.ContractDeactivatedRequest;
import clm.negotiation.dto.responses.NegotiationResponseDTO;
import clm.negotiation.mappers.NegotiationMapper;
import clm.negotiation.models.Negotiation;
import clm.negotiation.models.TerminatedContract;
import clm.negotiation.models.enums.NegotiationStatus;
import clm.negotiation.repositories.NegotiationRepository;
import clm.negotiation.repositories.TerminatedContractRepository;
import clm.negotiation.support.TestDataFactory;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractLifecycleServiceTest {

    @Mock NegotiationRepository        negotiationRepository;
    @Mock TerminatedContractRepository terminatedContractRepository;
    @Mock NegotiationMapper            mapper;

    @InjectMocks ContractLifecycleService service;

    // ================================================================== //
    //  onContractActivated                                                 //
    // ================================================================== //

    @Nested
    class OnContractActivated {

        @Test
        void saves_negotiation_and_returns_dto() {
            Negotiation saved = TestDataFactory.negotiation(1L, NegotiationStatus.ACCEPTED);
            NegotiationResponseDTO dto = TestDataFactory.response(1L, NegotiationStatus.ACCEPTED);
            when(negotiationRepository.save(any())).thenReturn(saved);
            when(mapper.toDto(saved)).thenReturn(dto);

            NegotiationResponseDTO result = service.onContractActivated(
                    new ContractActivatedRequest(10L, 42, LocalDate.of(2026, 1, 15),
                            LocalDate.of(2027, 1, 15), BigDecimal.valueOf(5_000)));

            assertThat(result.id()).isEqualTo(1L);
            verify(negotiationRepository).save(any());
        }

        @Test
        void negotiation_status_is_always_accepted() {
            when(negotiationRepository.save(any())).thenReturn(TestDataFactory.negotiation(1L, NegotiationStatus.ACCEPTED));
            when(mapper.toDto(any())).thenReturn(TestDataFactory.response(1L, NegotiationStatus.ACCEPTED));

            service.onContractActivated(
                    new ContractActivatedRequest(10L, 42, LocalDate.of(2026, 1, 15), null, null));

            ArgumentCaptor<Negotiation> captor = ArgumentCaptor.forClass(Negotiation.class);
            verify(negotiationRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(NegotiationStatus.ACCEPTED);
        }

        @Test
        void created_at_is_set_to_start_date_at_midnight() {
            when(negotiationRepository.save(any())).thenReturn(TestDataFactory.negotiation(1L, NegotiationStatus.ACCEPTED));
            when(mapper.toDto(any())).thenReturn(TestDataFactory.response(1L, NegotiationStatus.ACCEPTED));

            service.onContractActivated(
                    new ContractActivatedRequest(10L, 42, LocalDate.of(2026, 3, 10), null, null));

            ArgumentCaptor<Negotiation> captor = ArgumentCaptor.forClass(Negotiation.class);
            verify(negotiationRepository).save(captor.capture());
            assertThat(captor.getValue().getCreatedAt())
                    .isEqualTo(LocalDateTime.of(2026, 3, 10, 0, 0));
        }

        @Test
        void proposed_end_date_uses_end_date_when_provided() {
            when(negotiationRepository.save(any())).thenReturn(TestDataFactory.negotiation(1L, NegotiationStatus.ACCEPTED));
            when(mapper.toDto(any())).thenReturn(TestDataFactory.response(1L, NegotiationStatus.ACCEPTED));

            LocalDate endDate = LocalDate.of(2027, 6, 30);
            service.onContractActivated(
                    new ContractActivatedRequest(10L, 42, LocalDate.of(2026, 1, 1), endDate, null));

            ArgumentCaptor<Negotiation> captor = ArgumentCaptor.forClass(Negotiation.class);
            verify(negotiationRepository).save(captor.capture());
            assertThat(captor.getValue().getProposedEndDate()).isEqualTo(endDate);
        }

        @Test
        void proposed_end_date_falls_back_to_start_date_when_end_date_absent() {
            when(negotiationRepository.save(any())).thenReturn(TestDataFactory.negotiation(1L, NegotiationStatus.ACCEPTED));
            when(mapper.toDto(any())).thenReturn(TestDataFactory.response(1L, NegotiationStatus.ACCEPTED));

            LocalDate startDate = LocalDate.of(2026, 1, 1);
            service.onContractActivated(
                    new ContractActivatedRequest(10L, 42, startDate, null, null));

            ArgumentCaptor<Negotiation> captor = ArgumentCaptor.forClass(Negotiation.class);
            verify(negotiationRepository).save(captor.capture());
            assertThat(captor.getValue().getProposedEndDate()).isEqualTo(startDate);
        }

        @Test
        void contract_value_is_saved_when_provided() {
            when(negotiationRepository.save(any())).thenReturn(TestDataFactory.negotiation(1L, NegotiationStatus.ACCEPTED));
            when(mapper.toDto(any())).thenReturn(TestDataFactory.response(1L, NegotiationStatus.ACCEPTED));

            BigDecimal value = BigDecimal.valueOf(12_000);
            service.onContractActivated(
                    new ContractActivatedRequest(10L, 42, LocalDate.of(2026, 1, 1), null, value));

            ArgumentCaptor<Negotiation> captor = ArgumentCaptor.forClass(Negotiation.class);
            verify(negotiationRepository).save(captor.capture());
            assertThat(captor.getValue().getProposedValue()).isEqualByComparingTo(value);
        }

        @Test
        void contract_value_is_null_when_not_provided() {
            when(negotiationRepository.save(any())).thenReturn(TestDataFactory.negotiation(1L, NegotiationStatus.ACCEPTED));
            when(mapper.toDto(any())).thenReturn(TestDataFactory.response(1L, NegotiationStatus.ACCEPTED));

            service.onContractActivated(
                    new ContractActivatedRequest(10L, 42, LocalDate.of(2026, 1, 1), null, null));

            ArgumentCaptor<Negotiation> captor = ArgumentCaptor.forClass(Negotiation.class);
            verify(negotiationRepository).save(captor.capture());
            assertThat(captor.getValue().getProposedValue()).isNull();
        }

        @Test
        void created_by_user_id_is_zero_for_system_generated_negotiations() {
            when(negotiationRepository.save(any())).thenReturn(TestDataFactory.negotiation(1L, NegotiationStatus.ACCEPTED));
            when(mapper.toDto(any())).thenReturn(TestDataFactory.response(1L, NegotiationStatus.ACCEPTED));

            service.onContractActivated(
                    new ContractActivatedRequest(10L, 42, LocalDate.of(2026, 1, 1), null, null));

            ArgumentCaptor<Negotiation> captor = ArgumentCaptor.forClass(Negotiation.class);
            verify(negotiationRepository).save(captor.capture());
            assertThat(captor.getValue().getCreatedByUserId()).isEqualTo(0);
        }
    }

    // ================================================================== //
    //  onContractsDeactivated                                              //
    // ================================================================== //

    @Nested
    class OnContractsDeactivated {

        @Test
        void saves_terminated_contract_for_each_new_id() {
            when(terminatedContractRepository.existsByContractId(any())).thenReturn(false);

            service.onContractsDeactivated(new ContractDeactivatedRequest(List.of(1L, 2L, 3L)));

            verify(terminatedContractRepository, times(3)).save(any(TerminatedContract.class));
        }

        @Test
        void skips_contract_id_already_marked_as_terminated() {
            when(terminatedContractRepository.existsByContractId(1L)).thenReturn(true);
            when(terminatedContractRepository.existsByContractId(2L)).thenReturn(false);

            service.onContractsDeactivated(new ContractDeactivatedRequest(List.of(1L, 2L)));

            verify(terminatedContractRepository, times(1)).save(any(TerminatedContract.class));
        }

        @Test
        void saved_entity_has_correct_contract_id() {
            when(terminatedContractRepository.existsByContractId(99L)).thenReturn(false);

            service.onContractsDeactivated(new ContractDeactivatedRequest(List.of(99L)));

            ArgumentCaptor<TerminatedContract> captor = ArgumentCaptor.forClass(TerminatedContract.class);
            verify(terminatedContractRepository).save(captor.capture());
            assertThat(captor.getValue().getContractId()).isEqualTo(99L);
        }

        @Test
        void does_not_save_when_all_ids_already_terminated() {
            when(terminatedContractRepository.existsByContractId(any())).thenReturn(true);

            service.onContractsDeactivated(new ContractDeactivatedRequest(List.of(1L, 2L)));

            verify(terminatedContractRepository, never()).save(any());
        }
    }
}

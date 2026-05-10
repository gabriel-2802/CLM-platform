package clm.negotiation.services;

import clm.negotiation.dto.requests.CreateNegotiationRequest;
import clm.negotiation.dto.requests.UpdateNotesRequest;
import clm.negotiation.dto.responses.NegotiationResponseDTO;
import clm.negotiation.exceptions.exceptions.InvalidNegotiationStateException;
import clm.negotiation.exceptions.exceptions.ResourceNotFoundException;
import clm.negotiation.mappers.NegotiationMapper;
import clm.negotiation.models.Negotiation;
import clm.negotiation.models.enums.NegotiationStatus;
import clm.negotiation.repositories.NegotiationRepository;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NegotiationServiceTest {

    @Mock NegotiationRepository repository;
    @Mock NegotiationMapper     mapper;
    @Mock ContractApiClient     contractApiClient;

    @InjectMocks NegotiationService service;

    // ================================================================== //
    //  create                                                              //
    // ================================================================== //

    @Nested
    class Create {

        @Test
        void saves_negotiation_with_draft_status_and_returns_dto() {
            Negotiation saved = TestDataFactory.negotiation(1L, NegotiationStatus.DRAFT);
            NegotiationResponseDTO dto = TestDataFactory.response(1L, NegotiationStatus.DRAFT);

            when(repository.save(any())).thenReturn(saved);
            when(mapper.toDto(saved)).thenReturn(dto);

            NegotiationResponseDTO result = service.create(TestDataFactory.createRequest());

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.status()).isEqualTo(NegotiationStatus.DRAFT);
            verify(repository).save(any());
        }

        @Test
        void persisted_entity_has_draft_status_regardless_of_input() {
            Negotiation saved = TestDataFactory.negotiation(1L, NegotiationStatus.DRAFT);
            when(repository.save(any())).thenReturn(saved);
            when(mapper.toDto(any())).thenReturn(TestDataFactory.response(1L, NegotiationStatus.DRAFT));

            service.create(TestDataFactory.createRequest());

            ArgumentCaptor<Negotiation> captor = ArgumentCaptor.forClass(Negotiation.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(NegotiationStatus.DRAFT);
        }

        @Test
        void notes_are_persisted_when_provided() {
            Negotiation saved = TestDataFactory.negotiation(1L, NegotiationStatus.DRAFT);
            when(repository.save(any())).thenReturn(saved);
            when(mapper.toDto(any())).thenReturn(TestDataFactory.response(1L, NegotiationStatus.DRAFT));

            service.create(TestDataFactory.createRequest());

            ArgumentCaptor<Negotiation> captor = ArgumentCaptor.forClass(Negotiation.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getNotes()).isEqualTo("negociere trimestru Q2");
        }

        @Test
        void notes_are_null_when_not_provided() {
            Negotiation saved = TestDataFactory.negotiation(1L, NegotiationStatus.DRAFT);
            when(repository.save(any())).thenReturn(saved);
            when(mapper.toDto(any())).thenReturn(TestDataFactory.response(1L, NegotiationStatus.DRAFT));

            CreateNegotiationRequest req = new CreateNegotiationRequest(
                    10L, 42, BigDecimal.valueOf(4_500), LocalDate.of(2027, 6, 1), 7, null);
            service.create(req);

            ArgumentCaptor<Negotiation> captor = ArgumentCaptor.forClass(Negotiation.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getNotes()).isNull();
        }

        @Test
        void contract_api_client_is_not_called_on_create() {
            Negotiation saved = TestDataFactory.negotiation(1L, NegotiationStatus.DRAFT);
            when(repository.save(any())).thenReturn(saved);
            when(mapper.toDto(any())).thenReturn(TestDataFactory.response(1L, NegotiationStatus.DRAFT));

            service.create(TestDataFactory.createRequest());

            verifyNoInteractions(contractApiClient);
        }
    }

    // ================================================================== //
    //  getById                                                             //
    // ================================================================== //

    @Nested
    class GetById {

        @Test
        void returns_dto_when_found() {
            Negotiation n = TestDataFactory.negotiation(1L, NegotiationStatus.DRAFT);
            NegotiationResponseDTO dto = TestDataFactory.response(1L, NegotiationStatus.DRAFT);

            when(repository.findById(1L)).thenReturn(Optional.of(n));
            when(mapper.toDto(n)).thenReturn(dto);

            NegotiationResponseDTO result = service.getById(1L);

            assertThat(result.id()).isEqualTo(1L);
        }

        @Test
        void throws_resource_not_found_when_missing() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // ================================================================== //
    //  getByContractId                                                     //
    // ================================================================== //

    @Nested
    class GetByContractId {

        @Test
        void returns_all_negotiations_for_contract() {
            Negotiation n1 = TestDataFactory.negotiation(1L, NegotiationStatus.ACCEPTED);
            Negotiation n2 = TestDataFactory.negotiation(2L, NegotiationStatus.DRAFT);

            when(repository.findByContractIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(n1, n2));
            when(mapper.toDto(n1)).thenReturn(TestDataFactory.response(1L, NegotiationStatus.ACCEPTED));
            when(mapper.toDto(n2)).thenReturn(TestDataFactory.response(2L, NegotiationStatus.DRAFT));

            List<NegotiationResponseDTO> result = service.getByContractId(10L);

            assertThat(result).hasSize(2);
        }

        @Test
        void returns_empty_list_when_no_negotiations_exist() {
            when(repository.findByContractIdOrderByCreatedAtDesc(99L)).thenReturn(List.of());

            assertThat(service.getByContractId(99L)).isEmpty();
        }
    }

    // ================================================================== //
    //  getByClientId                                                       //
    // ================================================================== //

    @Nested
    class GetByClientId {

        @Test
        void returns_all_negotiations_for_client() {
            Negotiation n = TestDataFactory.negotiation(1L, NegotiationStatus.SENT);

            when(repository.findByClientIdOrderByCreatedAtDesc(42)).thenReturn(List.of(n));
            when(mapper.toDto(n)).thenReturn(TestDataFactory.response(1L, NegotiationStatus.SENT));

            List<NegotiationResponseDTO> result = service.getByClientId(42);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).status()).isEqualTo(NegotiationStatus.SENT);
        }
    }

    // ================================================================== //
    //  send                                                                //
    // ================================================================== //

    @Nested
    class Send {

        @Test
        void draft_negotiation_transitions_to_sent() {
            Negotiation n = TestDataFactory.negotiation(1L, NegotiationStatus.DRAFT);
            when(repository.findById(1L)).thenReturn(Optional.of(n));
            when(repository.save(n)).thenReturn(n);
            when(mapper.toDto(n)).thenReturn(TestDataFactory.response(1L, NegotiationStatus.SENT));

            service.send(1L);

            assertThat(n.getStatus()).isEqualTo(NegotiationStatus.SENT);
        }

        @Test
        void not_found_throws_resource_not_found_exception() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.send(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void sent_negotiation_cannot_be_sent_again() {
            Negotiation n = TestDataFactory.negotiation(1L, NegotiationStatus.SENT);
            when(repository.findById(1L)).thenReturn(Optional.of(n));

            assertThatThrownBy(() -> service.send(1L))
                    .isInstanceOf(InvalidNegotiationStateException.class)
                    .hasMessageContaining("DRAFT");
        }

        @Test
        void accepted_negotiation_cannot_be_sent() {
            Negotiation n = TestDataFactory.negotiation(1L, NegotiationStatus.ACCEPTED);
            when(repository.findById(1L)).thenReturn(Optional.of(n));

            assertThatThrownBy(() -> service.send(1L))
                    .isInstanceOf(InvalidNegotiationStateException.class);
        }

        @Test
        void rejected_negotiation_cannot_be_sent() {
            Negotiation n = TestDataFactory.negotiation(1L, NegotiationStatus.REJECTED);
            when(repository.findById(1L)).thenReturn(Optional.of(n));

            assertThatThrownBy(() -> service.send(1L))
                    .isInstanceOf(InvalidNegotiationStateException.class);
        }
    }

    // ================================================================== //
    //  accept                                                              //
    // ================================================================== //

    @Nested
    class Accept {

        @Test
        void sent_negotiation_transitions_to_accepted_and_updates_contract() {
            Negotiation n = TestDataFactory.negotiation(1L, NegotiationStatus.SENT);
            when(repository.findById(1L)).thenReturn(Optional.of(n));
            when(repository.save(n)).thenReturn(n);
            when(mapper.toDto(n)).thenReturn(TestDataFactory.resolvedResponse(1L, NegotiationStatus.ACCEPTED));

            service.accept(1L);

            assertThat(n.getStatus()).isEqualTo(NegotiationStatus.ACCEPTED);
            assertThat(n.getResolvedAt()).isNotNull();
            verify(contractApiClient).applyRenegotiation(
                    eq(10L),
                    eq(BigDecimal.valueOf(4_500)),
                    eq(LocalDate.of(2027, 6, 1))
            );
        }

        @Test
        void resolved_at_is_set_on_accept() {
            Negotiation n = TestDataFactory.negotiation(1L, NegotiationStatus.SENT);
            when(repository.findById(1L)).thenReturn(Optional.of(n));
            when(repository.save(n)).thenReturn(n);
            when(mapper.toDto(n)).thenReturn(TestDataFactory.resolvedResponse(1L, NegotiationStatus.ACCEPTED));

            service.accept(1L);

            assertThat(n.getResolvedAt()).isNotNull();
        }

        @Test
        void draft_negotiation_cannot_be_accepted() {
            Negotiation n = TestDataFactory.negotiation(1L, NegotiationStatus.DRAFT);
            when(repository.findById(1L)).thenReturn(Optional.of(n));

            assertThatThrownBy(() -> service.accept(1L))
                    .isInstanceOf(InvalidNegotiationStateException.class)
                    .hasMessageContaining("SENT");
        }

        @Test
        void already_accepted_negotiation_cannot_be_accepted_again() {
            Negotiation n = TestDataFactory.negotiation(1L, NegotiationStatus.ACCEPTED);
            when(repository.findById(1L)).thenReturn(Optional.of(n));

            assertThatThrownBy(() -> service.accept(1L))
                    .isInstanceOf(InvalidNegotiationStateException.class);
        }

        @Test
        void contract_api_not_called_when_negotiation_not_found() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.accept(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
            verifyNoInteractions(contractApiClient);
        }

        @Test
        void accept_with_only_value_calls_contract_api_with_null_end_date() {
            Negotiation n = TestDataFactory.negotiationWithoutEndDate(1L, NegotiationStatus.SENT);
            when(repository.findById(1L)).thenReturn(Optional.of(n));
            when(repository.save(n)).thenReturn(n);
            when(mapper.toDto(n)).thenReturn(TestDataFactory.resolvedResponse(1L, NegotiationStatus.ACCEPTED));

            service.accept(1L);

            verify(contractApiClient).applyRenegotiation(eq(10L), eq(BigDecimal.valueOf(4_500)), isNull());
        }

        @Test
        void accept_with_only_end_date_calls_contract_api_with_null_value() {
            Negotiation n = TestDataFactory.negotiationWithoutValue(1L, NegotiationStatus.SENT);
            when(repository.findById(1L)).thenReturn(Optional.of(n));
            when(repository.save(n)).thenReturn(n);
            when(mapper.toDto(n)).thenReturn(TestDataFactory.resolvedResponse(1L, NegotiationStatus.ACCEPTED));

            service.accept(1L);

            verify(contractApiClient).applyRenegotiation(
                    eq(10L), isNull(), eq(LocalDate.of(2027, 6, 1)));
        }
    }

    // ================================================================== //
    //  reject                                                              //
    // ================================================================== //

    @Nested
    class Reject {

        @Test
        void sent_negotiation_transitions_to_rejected() {
            Negotiation n = TestDataFactory.negotiation(1L, NegotiationStatus.SENT);
            when(repository.findById(1L)).thenReturn(Optional.of(n));
            when(repository.save(n)).thenReturn(n);
            when(mapper.toDto(n)).thenReturn(TestDataFactory.resolvedResponse(1L, NegotiationStatus.REJECTED));

            service.reject(1L, null);

            assertThat(n.getStatus()).isEqualTo(NegotiationStatus.REJECTED);
            assertThat(n.getResolvedAt()).isNotNull();
        }

        @Test
        void rejection_notes_override_existing_notes() {
            Negotiation n = TestDataFactory.negotiation(1L, NegotiationStatus.SENT);
            when(repository.findById(1L)).thenReturn(Optional.of(n));
            when(repository.save(n)).thenReturn(n);
            when(mapper.toDto(n)).thenReturn(TestDataFactory.resolvedResponse(1L, NegotiationStatus.REJECTED));

            service.reject(1L, new UpdateNotesRequest("pretul propus este prea mare"));

            assertThat(n.getNotes()).isEqualTo("pretul propus este prea mare");
        }

        @Test
        void rejection_without_notes_preserves_existing_notes() {
            Negotiation n = TestDataFactory.negotiation(1L, NegotiationStatus.SENT);
            when(repository.findById(1L)).thenReturn(Optional.of(n));
            when(repository.save(n)).thenReturn(n);
            when(mapper.toDto(n)).thenReturn(TestDataFactory.resolvedResponse(1L, NegotiationStatus.REJECTED));

            service.reject(1L, null);

            assertThat(n.getNotes()).isEqualTo("negociere trimestru Q2");
        }

        @Test
        void draft_negotiation_cannot_be_rejected() {
            Negotiation n = TestDataFactory.negotiation(1L, NegotiationStatus.DRAFT);
            when(repository.findById(1L)).thenReturn(Optional.of(n));

            assertThatThrownBy(() -> service.reject(1L, null))
                    .isInstanceOf(InvalidNegotiationStateException.class)
                    .hasMessageContaining("SENT");
        }

        @Test
        void already_rejected_negotiation_cannot_be_rejected_again() {
            Negotiation n = TestDataFactory.negotiation(1L, NegotiationStatus.REJECTED);
            when(repository.findById(1L)).thenReturn(Optional.of(n));

            assertThatThrownBy(() -> service.reject(1L, null))
                    .isInstanceOf(InvalidNegotiationStateException.class);
        }

        @Test
        void accepted_negotiation_cannot_be_rejected() {
            Negotiation n = TestDataFactory.negotiation(1L, NegotiationStatus.ACCEPTED);
            when(repository.findById(1L)).thenReturn(Optional.of(n));

            assertThatThrownBy(() -> service.reject(1L, null))
                    .isInstanceOf(InvalidNegotiationStateException.class);
        }

        @Test
        void contract_api_client_is_never_called_on_reject() {
            Negotiation n = TestDataFactory.negotiation(1L, NegotiationStatus.SENT);
            when(repository.findById(1L)).thenReturn(Optional.of(n));
            when(repository.save(n)).thenReturn(n);
            when(mapper.toDto(n)).thenReturn(TestDataFactory.resolvedResponse(1L, NegotiationStatus.REJECTED));

            service.reject(1L, null);

            verifyNoInteractions(contractApiClient);
        }
    }

    // ================================================================== //
    //  updateNotes                                                         //
    // ================================================================== //

    @Nested
    class UpdateNotes {

        @Test
        void updates_notes_on_any_status() {
            Negotiation n = TestDataFactory.negotiation(1L, NegotiationStatus.DRAFT);
            when(repository.findById(1L)).thenReturn(Optional.of(n));
            when(repository.save(n)).thenReturn(n);
            when(mapper.toDto(n)).thenReturn(TestDataFactory.response(1L, NegotiationStatus.DRAFT));

            service.updateNotes(1L, new UpdateNotesRequest("note noua"));

            assertThat(n.getNotes()).isEqualTo("note noua");
            verify(repository).save(n);
        }

        @Test
        void clears_notes_when_set_to_null() {
            Negotiation n = TestDataFactory.negotiation(1L, NegotiationStatus.DRAFT);
            when(repository.findById(1L)).thenReturn(Optional.of(n));
            when(repository.save(n)).thenReturn(n);
            when(mapper.toDto(n)).thenReturn(TestDataFactory.response(1L, NegotiationStatus.DRAFT));

            service.updateNotes(1L, new UpdateNotesRequest(null));

            assertThat(n.getNotes()).isNull();
        }

        @Test
        void throws_resource_not_found_when_missing() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateNotes(99L, new UpdateNotesRequest("orice")))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}

package clm.negotiation.controllers;

import clm.negotiation.dto.responses.NegotiationResponseDTO;
import clm.negotiation.exceptions.GlobalExceptionHandler;
import clm.negotiation.exceptions.exceptions.InvalidNegotiationStateException;
import clm.negotiation.exceptions.exceptions.ResourceNotFoundException;
import clm.negotiation.models.enums.NegotiationStatus;
import clm.negotiation.services.NegotiationService;
import clm.negotiation.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class NegotiationControllerTest {

    @Mock NegotiationService service;

    @InjectMocks NegotiationController controller;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    // ================================================================== //
    //  POST /api/negotiations                                              //
    // ================================================================== //

    @Nested
    class CreateNegotiation {

        @Test
        void valid_request_returns_201_with_location_header() throws Exception {
            NegotiationResponseDTO dto = TestDataFactory.response(1L, NegotiationStatus.DRAFT);
            when(service.create(any())).thenReturn(dto);

            mockMvc.perform(post("/api/negotiations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "contractId": 10,
                                  "clientId": 42,
                                  "proposedValue": 4500.00,
                                  "proposedEndDate": "2027-06-01",
                                  "createdByUserId": 7,
                                  "notes": "negociere trimestru Q2"
                                }
                                """))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString("/api/negotiations/1")))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.status").value("DRAFT"))
                    .andExpect(jsonPath("$.contractId").value(10))
                    .andExpect(jsonPath("$.clientId").value(42));
        }

        @Test
        void missing_contract_id_returns_400() throws Exception {
            mockMvc.perform(post("/api/negotiations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "clientId": 42,
                                  "proposedValue": 4500.00,
                                  "createdByUserId": 7
                                }
                                """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void missing_client_id_returns_400() throws Exception {
            mockMvc.perform(post("/api/negotiations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "contractId": 10,
                                  "proposedValue": 4500.00,
                                  "createdByUserId": 7
                                }
                                """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void missing_created_by_user_id_returns_400() throws Exception {
            mockMvc.perform(post("/api/negotiations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "contractId": 10,
                                  "clientId": 42,
                                  "proposedValue": 4500.00
                                }
                                """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void negative_proposed_value_returns_400() throws Exception {
            mockMvc.perform(post("/api/negotiations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "contractId": 10,
                                  "clientId": 42,
                                  "proposedValue": -100.00,
                                  "createdByUserId": 7
                                }
                                """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void notes_exceeding_2000_chars_returns_400() throws Exception {
            String longNotes = "x".repeat(2001);

            mockMvc.perform(post("/api/negotiations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "contractId": 10,
                                  "clientId": 42,
                                  "proposedValue": 4500.00,
                                  "createdByUserId": 7,
                                  "notes": "%s"
                                }
                                """.formatted(longNotes)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void request_without_notes_returns_201() throws Exception {
            NegotiationResponseDTO dto = TestDataFactory.response(1L, NegotiationStatus.DRAFT);
            when(service.create(any())).thenReturn(dto);

            mockMvc.perform(post("/api/negotiations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "contractId": 10,
                                  "clientId": 42,
                                  "proposedValue": 4500.00,
                                  "createdByUserId": 7
                                }
                                """))
                    .andExpect(status().isCreated());
        }
    }

    // ================================================================== //
    //  GET /api/negotiations/{id}                                          //
    // ================================================================== //

    @Nested
    class GetById {

        @Test
        void found_returns_200_with_negotiation() throws Exception {
            NegotiationResponseDTO dto = TestDataFactory.response(1L, NegotiationStatus.DRAFT);
            when(service.getById(1L)).thenReturn(dto);

            mockMvc.perform(get("/api/negotiations/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.status").value("DRAFT"))
                    .andExpect(jsonPath("$.notes").value("negociere trimestru Q2"));
        }

        @Test
        void not_found_returns_404() throws Exception {
            when(service.getById(99L)).thenThrow(new ResourceNotFoundException("Negotiation not found: 99"));

            mockMvc.perform(get("/api/negotiations/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // ================================================================== //
    //  GET /api/negotiations/contract/{contractId}                         //
    // ================================================================== //

    @Nested
    class GetByContractId {

        @Test
        void returns_200_with_list() throws Exception {
            List<NegotiationResponseDTO> list = List.of(
                    TestDataFactory.response(1L, NegotiationStatus.ACCEPTED),
                    TestDataFactory.response(2L, NegotiationStatus.DRAFT)
            );
            when(service.getByContractId(10L)).thenReturn(list);

            mockMvc.perform(get("/api/negotiations/contract/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].status").value("ACCEPTED"))
                    .andExpect(jsonPath("$[1].status").value("DRAFT"));
        }

        @Test
        void returns_204_when_no_negotiations_exist() throws Exception {
            when(service.getByContractId(99L)).thenReturn(List.of());

            mockMvc.perform(get("/api/negotiations/contract/99"))
                    .andExpect(status().isNoContent());
        }
    }

    // ================================================================== //
    //  GET /api/negotiations/client/{clientId}                             //
    // ================================================================== //

    @Nested
    class GetByClientId {

        @Test
        void returns_200_with_list() throws Exception {
            when(service.getByClientId(42)).thenReturn(
                    List.of(TestDataFactory.response(1L, NegotiationStatus.SENT)));

            mockMvc.perform(get("/api/negotiations/client/42"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].clientId").value(42));
        }

        @Test
        void returns_204_when_client_has_no_negotiations() throws Exception {
            when(service.getByClientId(99)).thenReturn(List.of());

            mockMvc.perform(get("/api/negotiations/client/99"))
                    .andExpect(status().isNoContent());
        }
    }


    // ================================================================== //
    //  PATCH /api/negotiations/{id}/accept                                 //
    // ================================================================== //

    @Nested
    class Accept {

        @Test
        void sent_transitions_to_accepted_returns_200() throws Exception {
            NegotiationResponseDTO dto = TestDataFactory.resolvedResponse(1L, NegotiationStatus.ACCEPTED);
            when(service.accept(1L)).thenReturn(dto);

            mockMvc.perform(patch("/api/negotiations/1/accept"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ACCEPTED"))
                    .andExpect(jsonPath("$.resolvedAt").isNotEmpty());
        }

        @Test
        void not_found_returns_404() throws Exception {
            when(service.accept(99L)).thenThrow(new ResourceNotFoundException("Negotiation not found: 99"));

            mockMvc.perform(patch("/api/negotiations/99/accept"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void wrong_state_returns_409() throws Exception {
            when(service.accept(1L)).thenThrow(
                    new InvalidNegotiationStateException("Only SENT negotiations can be accepted. Current status: DRAFT"));

            mockMvc.perform(patch("/api/negotiations/1/accept"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.detail").value(containsString("DRAFT")));
        }
    }

    // ================================================================== //
    //  PATCH /api/negotiations/{id}/reject                                 //
    // ================================================================== //

    @Nested
    class Reject {

        @Test
        void sent_transitions_to_rejected_returns_200() throws Exception {
            NegotiationResponseDTO dto = TestDataFactory.resolvedResponse(1L, NegotiationStatus.REJECTED);
            when(service.reject(eq(1L), any())).thenReturn(dto);

            mockMvc.perform(patch("/api/negotiations/1/reject")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                { "notes": "pretul propus este prea mare" }
                                """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("REJECTED"));
        }

        @Test
        void reject_without_body_returns_200() throws Exception {
            NegotiationResponseDTO dto = TestDataFactory.resolvedResponse(1L, NegotiationStatus.REJECTED);
            when(service.reject(eq(1L), isNull())).thenReturn(dto);

            mockMvc.perform(patch("/api/negotiations/1/reject"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("REJECTED"));
        }

        @Test
        void not_found_returns_404() throws Exception {
            when(service.reject(eq(99L), any())).thenThrow(new ResourceNotFoundException("Negotiation not found: 99"));

            mockMvc.perform(patch("/api/negotiations/99/reject"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void wrong_state_returns_409() throws Exception {
            when(service.reject(eq(1L), any())).thenThrow(
                    new InvalidNegotiationStateException("Only SENT negotiations can be rejected. Current status: DRAFT"));

            mockMvc.perform(patch("/api/negotiations/1/reject"))
                    .andExpect(status().isConflict());
        }
    }

    // ================================================================== //
    //  PATCH /api/negotiations/{id}/notes                                  //
    // ================================================================== //

    @Nested
    class UpdateNotes {

        @Test
        void valid_notes_return_200() throws Exception {
            NegotiationResponseDTO dto = TestDataFactory.response(1L, NegotiationStatus.DRAFT);
            when(service.updateNotes(eq(1L), any())).thenReturn(dto);

            mockMvc.perform(patch("/api/negotiations/1/notes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                { "notes": "nota actualizata dupa discutia cu clientul" }
                                """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        void notes_exceeding_2000_chars_returns_400() throws Exception {
            String longNotes = "x".repeat(2001);

            mockMvc.perform(patch("/api/negotiations/1/notes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                { "notes": "%s" }
                                """.formatted(longNotes)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void not_found_returns_404() throws Exception {
            when(service.updateNotes(eq(99L), any()))
                    .thenThrow(new ResourceNotFoundException("Negotiation not found: 99"));

            mockMvc.perform(patch("/api/negotiations/99/notes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                { "notes": "orice" }
                                """))
                    .andExpect(status().isNotFound());
        }

        @Test
        void null_notes_in_body_returns_200() throws Exception {
            NegotiationResponseDTO dto = TestDataFactory.response(1L, NegotiationStatus.DRAFT);
            when(service.updateNotes(eq(1L), any())).thenReturn(dto);

            mockMvc.perform(patch("/api/negotiations/1/notes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                { "notes": null }
                                """))
                    .andExpect(status().isOk());
        }
    }
}

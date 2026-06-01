package clm.negotiation.controllers;

import clm.negotiation.dto.responses.NegotiationResponseDTO;
import clm.negotiation.exceptions.GlobalExceptionHandler;
import clm.negotiation.models.enums.NegotiationStatus;
import clm.negotiation.services.ContractLifecycleService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ContractLifecycleControllerTest {

    @Mock ContractLifecycleService lifecycleService;

    @InjectMocks ContractLifecycleController controller;

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
    //  POST /api/negotiations/contracts/activated                          //
    // ================================================================== //

    @Nested
    class ContractActivated {

        @Test
        void valid_full_request_returns_200_with_negotiation_dto() throws Exception {
            NegotiationResponseDTO dto = TestDataFactory.response(1L, NegotiationStatus.ACCEPTED);
            when(lifecycleService.onContractActivated(any())).thenReturn(dto);

            mockMvc.perform(post("/api/negotiations/contracts/activated")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "contractId": 10,
                                  "clientId": 42,
                                  "startDate": "2026-01-15",
                                  "endDate": "2027-01-15",
                                  "contractValue": 5000.00
                                }
                                """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.status").value("ACCEPTED"))
                    .andExpect(jsonPath("$.contractId").value(10))
                    .andExpect(jsonPath("$.clientId").value(42));
        }

        @Test
        void request_without_optional_end_date_and_value_returns_200() throws Exception {
            NegotiationResponseDTO dto = TestDataFactory.response(1L, NegotiationStatus.ACCEPTED);
            when(lifecycleService.onContractActivated(any())).thenReturn(dto);

            mockMvc.perform(post("/api/negotiations/contracts/activated")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "contractId": 10,
                                  "clientId": 42,
                                  "startDate": "2026-01-15"
                                }
                                """))
                    .andExpect(status().isOk());
        }

        @Test
        void missing_contract_id_returns_400() throws Exception {
            mockMvc.perform(post("/api/negotiations/contracts/activated")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "clientId": 42,
                                  "startDate": "2026-01-15"
                                }
                                """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void missing_client_id_returns_400() throws Exception {
            mockMvc.perform(post("/api/negotiations/contracts/activated")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "contractId": 10,
                                  "startDate": "2026-01-15"
                                }
                                """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void missing_start_date_returns_400() throws Exception {
            mockMvc.perform(post("/api/negotiations/contracts/activated")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "contractId": 10,
                                  "clientId": 42
                                }
                                """))
                    .andExpect(status().isBadRequest());
        }
    }

    // ================================================================== //
    //  POST /api/negotiations/contracts/deactivated                        //
    // ================================================================== //

    @Nested
    class ContractDeactivated {

        @Test
        void valid_request_with_multiple_ids_returns_200() throws Exception {
            doNothing().when(lifecycleService).onContractsDeactivated(any());

            mockMvc.perform(post("/api/negotiations/contracts/deactivated")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "contractIds": [1, 2, 3]
                                }
                                """))
                    .andExpect(status().isOk());
        }

        @Test
        void valid_request_with_single_id_returns_200() throws Exception {
            doNothing().when(lifecycleService).onContractsDeactivated(any());

            mockMvc.perform(post("/api/negotiations/contracts/deactivated")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "contractIds": [99]
                                }
                                """))
                    .andExpect(status().isOk());
        }

        @Test
        void empty_contract_ids_list_returns_400() throws Exception {
            mockMvc.perform(post("/api/negotiations/contracts/deactivated")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "contractIds": []
                                }
                                """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void missing_contract_ids_field_returns_400() throws Exception {
            mockMvc.perform(post("/api/negotiations/contracts/deactivated")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {}
                                """))
                    .andExpect(status().isBadRequest());
        }
    }
}

package clm.demo.controllers;

import clm.demo.dto.responses.ContractResponseDTO;
import clm.demo.exceptions.GlobalExceptionHandler;
import clm.demo.exceptions.exceptions.InvalidContractStateException;
import clm.demo.exceptions.exceptions.ResourceNotFoundException;
import clm.demo.services.ContractService;
import clm.demo.services.download.DocumentDownloadService;
import clm.demo.support.TestDataFactory;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ContractControllerTest {

    @Mock ContractService         contractService;
    @Mock DocumentDownloadService downloadService;

    @InjectMocks ContractController controller;

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
    //  PATCH /api/contracts/{id}/renegotiate                               //
    // ================================================================== //

//    @Nested
//    class RenegotiateContract {
//
//        @Test
//        void valid_request_updates_value_and_end_date_returns_200() throws Exception {
//            ContractResponseDTO dto = TestDataFactory.contractResponse(1L, "ACTIVE");
//            when(contractService.renegotiateContract(eq(1L), any())).thenReturn(dto);
//
//            mockMvc.perform(patch("/api/contracts/1/renegotiate")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content("""
//                                {
//                                  "contractValue": 15000.00,
//                                  "contractEndDate": "2028-06-01"
//                                }
//                                """))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.id").value(1));
//        }
//
//        @Test
//        void request_with_only_value_returns_200() throws Exception {
//            ContractResponseDTO dto = TestDataFactory.contractResponse(1L, "ACTIVE");
//            when(contractService.renegotiateContract(eq(1L), any())).thenReturn(dto);
//
//            mockMvc.perform(patch("/api/contracts/1/renegotiate")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content("""
//                                { "contractValue": 20000.00 }
//                                """))
//                    .andExpect(status().isOk());
//        }
//
//        @Test
//        void request_with_only_end_date_returns_200() throws Exception {
//            ContractResponseDTO dto = TestDataFactory.contractResponse(1L, "ACTIVE");
//            when(contractService.renegotiateContract(eq(1L), any())).thenReturn(dto);
//
//            mockMvc.perform(patch("/api/contracts/1/renegotiate")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content("""
//                                { "contractEndDate": "2029-01-01" }
//                                """))
//                    .andExpect(status().isOk());
//        }

//        @Test
//        void contract_not_found_returns_404() throws Exception {
//            when(contractService.renegotiateContract(eq(99L), any()))
//                    .thenThrow(new ResourceNotFoundException("Contract not found: 99"));
//
//            mockMvc.perform(patch("/api/contracts/99/renegotiate")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content("""
//                                { "contractValue": 5000.00 }
//                                """))
//                    .andExpect(status().isNotFound());
//        }

//        @Test
//        void non_active_contract_returns_409() throws Exception {
//            when(contractService.renegotiateContract(eq(1L), any()))
//                    .thenThrow(new InvalidContractStateException(
//                            "Cannot renegotiate contract in status: TERMINATED. Only ACTIVE contracts can be renegotiated."));
//
//            mockMvc.perform(patch("/api/contracts/1/renegotiate")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content("""
//                                { "contractValue": 5000.00 }
//                                """))
//                    .andExpect(status().isConflict());
//        }
//    }
}

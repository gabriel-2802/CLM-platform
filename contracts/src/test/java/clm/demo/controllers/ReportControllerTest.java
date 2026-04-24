package clm.demo.controllers;

import clm.demo.dto.responses.ContractResponseDTO;
import clm.demo.services.ReportService;
import clm.demo.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock  ReportService    reportService;

    @InjectMocks ReportController controller;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // LocalValidatorFactoryBean enables @Min(1) validation on @RequestParam in standaloneSetup
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    // ================================================================== //
    //  GET /api/contracts/report/expiring                                  //
    // ================================================================== //

    @Nested
    class GetExpiringContracts {

        @Test
        void returns_200_with_contracts() throws Exception {
            ContractResponseDTO dto = TestDataFactory.contractResponse(1L, "ACTIVE");
            when(reportService.getExpiringContracts(anyInt())).thenReturn(List.of(dto));

            mockMvc.perform(get("/api/contracts/report/expiring").param("days", "30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].contractStatus").value("ACTIVE"));
        }

        @Test
        void empty_result_returns_204() throws Exception {
            when(reportService.getExpiringContracts(anyInt())).thenReturn(List.of());

            mockMvc.perform(get("/api/contracts/report/expiring").param("days", "30"))
                    .andExpect(status().isNoContent());
        }

        @Test
        void missing_days_param_returns_400() throws Exception {
            // days is a required @RequestParam with no default value
            mockMvc.perform(get("/api/contracts/report/expiring"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ================================================================== //
    //  GET /api/contracts/report/inactive-clients                          //
    // ================================================================== //

    @Nested
    class GetInactiveClientContracts {

        @Test
        void returns_200_with_contracts() throws Exception {
            ContractResponseDTO dto = TestDataFactory.contractResponse(2L, "ACTIVE");
            when(reportService.getInactiveClientContracts(anyInt())).thenReturn(List.of(dto));

            mockMvc.perform(get("/api/contracts/report/inactive-clients").param("months", "6"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(2));
        }

        @Test
        void empty_result_returns_204() throws Exception {
            when(reportService.getInactiveClientContracts(anyInt())).thenReturn(List.of());

            mockMvc.perform(get("/api/contracts/report/inactive-clients").param("months", "6"))
                    .andExpect(status().isNoContent());
        }

        @Test
        void missing_months_param_returns_400() throws Exception {
            mockMvc.perform(get("/api/contracts/report/inactive-clients"))
                    .andExpect(status().isBadRequest());
        }
    }
}

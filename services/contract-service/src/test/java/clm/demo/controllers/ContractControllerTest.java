package clm.demo.controllers;

import clm.demo.dto.responses.ContractResponseDTO;
import clm.demo.exceptions.*;
import clm.demo.exceptions.exceptions.InvalidContractStateException;
import clm.demo.exceptions.exceptions.MissingMandatoryFieldException;
import clm.demo.exceptions.exceptions.ResourceNotFoundException;
import clm.demo.exceptions.exceptions.TemplateIncompleteException;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
    //  POST /api/contracts/generate                                        //
    // ================================================================== //

    @Nested
    class GenerateContract {

        @Test
        void valid_request_returns_201_with_location_header() throws Exception {
            ContractResponseDTO dto = TestDataFactory.contractResponse(88L, "PENDING_SIGNATURE");
            when(contractService.generateContract(any())).thenReturn(dto);

            String body = """
                {
                  "templateId": 1,
                  "userId": 10,
                  "userMail": "staff@company.com",
                  "clientId": 42,
                  "startDate": "2026-01-01",
                  "endDate": "2027-01-01",
                  "mappings": { "Client Name": "Acme Corp" },
                  "autoRenew": true,
                  "contractBalance": 5000
                }
                """;

            mockMvc.perform(post("/api/contracts/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString("/api/contracts/88")))
                    .andExpect(jsonPath("$.id").value(88))
                    .andExpect(jsonPath("$.contractStatus").value("PENDING_SIGNATURE"));
        }

        @Test
        void missing_required_fields_returns_400() throws Exception {
            // empty body — all @NotNull / @NotBlank fields fail validation
            mockMvc.perform(post("/api/contracts/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void template_not_found_returns_404() throws Exception {
            when(contractService.generateContract(any()))
                    .thenThrow(new ResourceNotFoundException("Template not found: 1"));

            String body = """
                {
                  "templateId": 1,
                  "userId": 10,
                  "userMail": "staff@company.com",
                  "clientId": 42,
                  "startDate": "2026-01-01",
                  "endDate": "2027-01-01",
                  "mappings": { "Client Name": "Acme Corp" },
                  "autoRenew": true,
                  "contractBalance": 5000
                }
                """;

            mockMvc.perform(post("/api/contracts/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound());
        }

        @Test
        void template_incomplete_returns_422() throws Exception {
            when(contractService.generateContract(any()))
                    .thenThrow(new TemplateIncompleteException("Template is not fully mapped"));

            String body = """
                {
                  "templateId": 1,
                  "userId": 10,
                  "userMail": "staff@company.com",
                  "clientId": 42,
                  "startDate": "2026-01-01",
                  "endDate": "2027-01-01",
                  "mappings": { "Client Name": "Acme Corp" },
                  "autoRenew": true,
                  "contractBalance": 5000
                }
                """;

            mockMvc.perform(post("/api/contracts/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        void missing_mandatory_field_mappings_returns_400() throws Exception {
            when(contractService.generateContract(any()))
                    .thenThrow(new MissingMandatoryFieldException(
                            "Missing mandatory fields", List.of("Client Name")));

            String body = """
                {
                  "templateId": 1,
                  "userId": 10,
                  "userMail": "staff@company.com",
                  "clientId": 42,
                  "startDate": "2026-01-01",
                  "endDate": "2027-01-01",
                  "mappings": {},
                  "autoRenew": true,
                  "contractBalance": 5000
                }
                """;

            mockMvc.perform(post("/api/contracts/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    // ================================================================== //
    //  POST /api/contracts/{contractId}/upload-signed                      //
    // ================================================================== //

    @Nested
    class UploadSignedContract {

        @Test
        void pdf_upload_returns_200_with_active_status() throws Exception {
            ContractResponseDTO dto = TestDataFactory.contractResponse(1L, "ACTIVE");
            when(contractService.uploadSignedContract(eq(1L), any(byte[].class), eq(42))).thenReturn(dto);

            MockMultipartFile file = new MockMultipartFile(
                    "file", "signed.pdf", "application/pdf", TestDataFactory.pdfMagicBytes());

            mockMvc.perform(multipart("/api/contracts/1/upload-signed")
                    .file(file)
                    .param("userId", "42"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.contractStatus").value("ACTIVE"));
        }

        @Test
        void empty_file_returns_400() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "empty.pdf", "application/pdf", new byte[0]);

            mockMvc.perform(multipart("/api/contracts/1/upload-signed")
                    .file(file)
                    .param("userId", "42"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void contract_not_found_returns_404() throws Exception {
            when(contractService.uploadSignedContract(eq(99L), any(byte[].class), eq(42)))
                    .thenThrow(new ResourceNotFoundException("Contract not found: 99"));

            MockMultipartFile file = new MockMultipartFile(
                    "file", "signed.pdf", "application/pdf", TestDataFactory.pdfMagicBytes());

            mockMvc.perform(multipart("/api/contracts/99/upload-signed")
                    .file(file)
                    .param("userId", "42"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void wrong_contract_state_returns_409() throws Exception {
            when(contractService.uploadSignedContract(eq(1L), any(byte[].class), eq(42)))
                    .thenThrow(new InvalidContractStateException(
                            "Contract is not in PENDING_SIGNATURE state"));

            MockMultipartFile file = new MockMultipartFile(
                    "file", "signed.pdf", "application/pdf", TestDataFactory.pdfMagicBytes());

            mockMvc.perform(multipart("/api/contracts/1/upload-signed")
                    .file(file)
                    .param("userId", "42"))
                    .andExpect(status().isConflict());
        }
    }

    // ================================================================== //
    //  PUT /api/contracts/terminate/{contractId}                           //
    // ================================================================== //

    @Nested
    class TerminateContract {

        @Test
        void valid_request_returns_204() throws Exception {
            doNothing().when(contractService).terminateContract(eq(1L), any());

            String body = """
                { "terminationDate": "2026-06-01", "reasons": "early exit" }
                """;

            mockMvc.perform(put("/api/contracts/terminate/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNoContent());
        }

        @Test
        void contract_not_found_returns_404() throws Exception {
            doThrow(new ResourceNotFoundException("Contract not found: 99"))
                    .when(contractService).terminateContract(eq(99L), any());

            String body = """
                { "terminationDate": "2026-06-01", "reasons": "early exit" }
                """;

            mockMvc.perform(put("/api/contracts/terminate/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound());
        }

        @Test
        void invalid_contract_state_returns_409() throws Exception {
            doThrow(new InvalidContractStateException("Contract is not ACTIVE"))
                    .when(contractService).terminateContract(eq(1L), any());

            String body = """
                { "terminationDate": "2026-06-01", "reasons": "early exit" }
                """;

            mockMvc.perform(put("/api/contracts/terminate/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isConflict());
        }

        @Test
        void missing_termination_date_returns_400() throws Exception {
            // terminationDate is @NotNull — omitting it triggers bean validation
            String body = """
                { "reasons": "early exit" }
                """;

            mockMvc.perform(put("/api/contracts/terminate/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    // ================================================================== //
    //  GET /api/contracts                                                  //
    // ================================================================== //

    @Nested
    class GetAll {

        @Test
        void returns_200_with_list() throws Exception {
            ContractResponseDTO dto = TestDataFactory.contractResponse(1L, "ACTIVE");
            when(contractService.getAll(anyInt(), anyInt()))
                    .thenReturn(new PageImpl<>(List.of(dto)));

            mockMvc.perform(get("/api/contracts/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].contractStatus").value("ACTIVE"));
        }

        @Test
        void empty_page_returns_204() throws Exception {
            when(contractService.getAll(anyInt(), anyInt()))
                    .thenReturn(new PageImpl<>(List.of()));

            mockMvc.perform(get("/api/contracts/all"))
                    .andExpect(status().isNoContent());
        }
    }

    // ================================================================== //
    //  POST /api/contracts/search                                          //
    // ================================================================== //

    @Nested
    class SearchContracts {

        @Test
        void matching_contracts_return_200() throws Exception {
            ContractResponseDTO dto = TestDataFactory.contractResponse(1L, "ACTIVE");
            when(contractService.search(any()))
                    .thenReturn(new PageImpl<>(List.of(dto)));

            mockMvc.perform(post("/api/contracts/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1));
        }

        @Test
        void no_matching_contracts_return_204() throws Exception {
            when(contractService.search(any()))
                    .thenReturn(new PageImpl<>(List.of()));

            mockMvc.perform(post("/api/contracts/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isNoContent());
        }
    }

    // ================================================================== //
    //  GET /api/contracts/download/{contractId}/{type}/{format}            //
    // ================================================================== //

    @Nested
    class DownloadContract {

        @Test
        void valid_request_returns_200_with_attachment_header() throws Exception {
            when(downloadService.downloadDocument(any(), any(), any()))
                    .thenReturn(new byte[]{1, 2, 3});

            mockMvc.perform(get("/api/contracts/download/1/unsigned/pdf"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition",
                            "attachment; filename=contract-1.pdf"));
        }

        @Test
        void invalid_format_returns_400() throws Exception {
            mockMvc.perform(get("/api/contracts/download/1/unsigned/xlsx"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void invalid_type_returns_400() throws Exception {
            mockMvc.perform(get("/api/contracts/download/1/original/pdf"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void contract_not_found_returns_404() throws Exception {
            when(downloadService.downloadDocument(any(), any(), any()))
                    .thenThrow(new ResourceNotFoundException("not found"));

            mockMvc.perform(get("/api/contracts/download/99/unsigned/pdf"))
                    .andExpect(status().isNotFound());
        }
    }
}

package clm.demo.integration;

import clm.demo.controllers.ContractController;
import clm.demo.dto.responses.ContractResponseDTO;
import clm.demo.exceptions.exceptions.InvalidContractStateException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ContractControllerIT extends AbstractControllerTest {

    @Mock ContractService         contractService;
    @Mock DocumentDownloadService downloadService;

    @InjectMocks ContractController controller;

    @BeforeEach
    void setUp() {
        buildMockMvc(controller);
    }

    // ── POST /api/contracts/generate ─────────────────────────────────────────

    @Nested
    class GenerateContract {

        @Test
        void should_return_201_and_location_header_when_contract_created() throws Exception {
            ContractResponseDTO dto = TestDataFactory.contractResponse(42L, "PENDING_SIGNATURE");
            when(contractService.generateContract(any())).thenReturn(dto);

            mockMvc.perform(post("/api/contracts/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validGenerateRequest()))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/contracts/42")))
                    .andExpect(jsonPath("$.id").value(42));
        }

        @Test
        void should_return_400_when_template_id_missing() throws Exception {
            mockMvc.perform(post("/api/contracts/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "userId": 1,
                                      "clientId": 10,
                                      "startDate": "2026-01-01",
                                      "endDate": "2027-01-01",
                                      "mappings": {},
                                      "autoRenew": false,
                                      "contractBalance": 5000
                                    }
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void should_return_404_when_template_not_found() throws Exception {
            when(contractService.generateContract(any()))
                    .thenThrow(new ResourceNotFoundException("Template not found: 999"));

            mockMvc.perform(post("/api/contracts/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validGenerateRequest()))
                    .andExpect(status().isNotFound());
        }

        @Test
        void should_return_422_when_template_not_fully_mapped() throws Exception {
            when(contractService.generateContract(any()))
                    .thenThrow(new TemplateIncompleteException("Template 1 is not fully mapped."));

            mockMvc.perform(post("/api/contracts/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validGenerateRequest()))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        void should_return_400_when_start_date_missing() throws Exception {
            mockMvc.perform(post("/api/contracts/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "templateId": 1,
                                      "userId": 1,
                                      "clientId": 10,
                                      "endDate": "2027-01-01",
                                      "mappings": {},
                                      "autoRenew": false,
                                      "contractBalance": 5000
                                    }
                                    """))
                    .andExpect(status().isBadRequest());
        }
    }

    // ── GET /api/contracts/{id} ───────────────────────────────────────────────

    @Nested
    class GetById {

        @Test
        void should_return_200_with_contract_when_found() throws Exception {
            ContractResponseDTO dto = TestDataFactory.contractResponse(1L, "ACTIVE");
            when(contractService.getById(1L)).thenReturn(dto);

            mockMvc.perform(get("/api/contracts/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.contractStatus").value("ACTIVE"));
        }

        @Test
        void should_return_404_when_contract_not_found() throws Exception {
            when(contractService.getById(99L))
                    .thenThrow(new ResourceNotFoundException("Contract not found: 99"));

            mockMvc.perform(get("/api/contracts/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // ── GET /api/contracts/all ────────────────────────────────────────────────

    @Nested
    class GetAll {

        @Test
        void should_return_200_with_list_when_contracts_exist() throws Exception {
            Page<ContractResponseDTO> page = new PageImpl<>(List.of(
                    TestDataFactory.contractResponse(1L, "ACTIVE"),
                    TestDataFactory.contractResponse(2L, "PENDING_SIGNATURE")
            ));
            when(contractService.getAll(0, 20)).thenReturn(page);

            mockMvc.perform(get("/api/contracts/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        void should_return_204_when_no_contracts_exist() throws Exception {
            when(contractService.getAll(0, 20)).thenReturn(Page.empty());

            mockMvc.perform(get("/api/contracts/all"))
                    .andExpect(status().isNoContent());
        }
    }

    // ── PUT /api/contracts/terminate/{id} ─────────────────────────────────────

    @Nested
    class TerminateContract {

        @Test
        void should_return_204_when_termination_succeeds() throws Exception {
            doNothing().when(contractService).terminateContract(eq(1L), any());

            mockMvc.perform(put("/api/contracts/terminate/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "terminationDate": "2026-06-01", "userId": 42, "reasons": "breach" }
                                    """))
                    .andExpect(status().isNoContent());
        }

        @Test
        void should_return_400_when_termination_date_missing() throws Exception {
            mockMvc.perform(put("/api/contracts/terminate/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "userId": 42 }
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void should_return_404_when_contract_not_found() throws Exception {
            doThrow(new ResourceNotFoundException("Contract not found: 99"))
                    .when(contractService).terminateContract(eq(99L), any());

            mockMvc.perform(put("/api/contracts/terminate/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "terminationDate": "2026-06-01" }
                                    """))
                    .andExpect(status().isNotFound());
        }

        @Test
        void should_return_409_when_contract_not_active() throws Exception {
            doThrow(new InvalidContractStateException(
                    "Cannot terminate contract in status: PENDING_SIGNATURE"))
                    .when(contractService).terminateContract(eq(1L), any());

            mockMvc.perform(put("/api/contracts/terminate/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "terminationDate": "2026-06-01" }
                                    """))
                    .andExpect(status().isConflict());
        }
    }

    // ── POST /api/contracts/{id}/upload-signed ─────────────────────────────────

    @Nested
    class UploadSignedContract {

        @Test
        void should_return_200_when_file_uploaded_successfully() throws Exception {
            ContractResponseDTO dto = TestDataFactory.contractResponse(1L, "ACTIVE");
            when(contractService.uploadSignedContract(eq(1L), any(), eq(42))).thenReturn(dto);

            MockMultipartFile file = new MockMultipartFile(
                    "file", "signed.pdf", "application/pdf",
                    new byte[]{0x25, 0x50, 0x44, 0x46}); // PDF magic bytes

            mockMvc.perform(multipart("/api/contracts/1/upload-signed")
                            .file(file)
                            .param("userId", "42"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.contractStatus").value("ACTIVE"));
        }

        @Test
        void should_return_400_when_file_is_empty() throws Exception {
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file", "empty.pdf", "application/pdf", new byte[0]);

            mockMvc.perform(multipart("/api/contracts/1/upload-signed")
                            .file(emptyFile)
                            .param("userId", "42"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void should_return_404_when_contract_not_found() throws Exception {
            when(contractService.uploadSignedContract(eq(99L), any(), anyInt()))
                    .thenThrow(new ResourceNotFoundException("Contract not found: 99"));

            MockMultipartFile file = new MockMultipartFile(
                    "file", "signed.pdf", "application/pdf",
                    new byte[]{0x25, 0x50, 0x44, 0x46});

            mockMvc.perform(multipart("/api/contracts/99/upload-signed")
                            .file(file)
                            .param("userId", "42"))
                    .andExpect(status().isNotFound());
        }
    }

    // ── PATCH /api/contracts/{id}/update-terms ─────────────────────────────────

    @Nested
    class UpdateContractTerms {

        @Test
        void should_return_200_when_terms_updated() throws Exception {
            ContractResponseDTO dto = TestDataFactory.contractResponse(1L, "ACTIVE");
            when(contractService.updateContractTerms(eq(1L), any())).thenReturn(dto);

            mockMvc.perform(patch("/api/contracts/1/update-terms")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "userId": 1,
                                      "appendixId": 5,
                                      "startDate": "2026-01-01",
                                      "contractEndDate": "2028-01-01"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        void should_return_404_when_contract_not_found() throws Exception {
            when(contractService.updateContractTerms(eq(99L), any()))
                    .thenThrow(new ResourceNotFoundException("Contract not found: 99"));

            mockMvc.perform(patch("/api/contracts/99/update-terms")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "userId": 1,
                                      "appendixId": 5,
                                      "startDate": "2026-01-01"
                                    }
                                    """))
                    .andExpect(status().isNotFound());
        }

        @Test
        void should_return_409_when_contract_not_active() throws Exception {
            when(contractService.updateContractTerms(eq(1L), any()))
                    .thenThrow(new InvalidContractStateException("Cannot update: not ACTIVE"));

            mockMvc.perform(patch("/api/contracts/1/update-terms")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "userId": 1,
                                      "appendixId": 5,
                                      "startDate": "2026-01-01"
                                    }
                                    """))
                    .andExpect(status().isConflict());
        }
    }

    // ── POST /api/contracts/search ────────────────────────────────────────────

    @Nested
    class SearchContracts {

        @Test
        void should_return_200_when_matches_found() throws Exception {
            Page<ContractResponseDTO> page = new PageImpl<>(
                    List.of(TestDataFactory.contractResponse(1L, "ACTIVE")));
            when(contractService.search(any())).thenReturn(page);

            mockMvc.perform(post("/api/contracts/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        void should_return_204_when_no_matches() throws Exception {
            when(contractService.search(any())).thenReturn(Page.empty());

            mockMvc.perform(post("/api/contracts/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isNoContent());
        }
    }

    // ── GET /api/contracts/download/{id}/{type}/{format} ──────────────────────

    @Nested
    class DownloadContract {

        @Test
        void should_return_400_when_format_invalid() throws Exception {
            mockMvc.perform(get("/api/contracts/download/1/unsigned/xyz"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void should_return_400_when_type_invalid() throws Exception {
            mockMvc.perform(get("/api/contracts/download/1/invalid/pdf"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void should_return_200_with_pdf_bytes_when_download_succeeds() throws Exception {
            byte[] content = new byte[]{1, 2, 3};
            when(downloadService.downloadDocument(eq(1L), any(), any())).thenReturn(content);

            mockMvc.perform(get("/api/contracts/download/1/unsigned/pdf"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition",
                            org.hamcrest.Matchers.containsString("contract-1.pdf")))
                    .andExpect(content().bytes(content));
        }
    }

    // ── GET /api/contracts/{id}/detailed ──────────────────────────────────────

    @Nested
    class GetDetailed {

        @Test
        void should_return_404_when_contract_not_found() throws Exception {
            when(contractService.getDetailedById(99L))
                    .thenThrow(new ResourceNotFoundException("Contract not found: 99"));

            mockMvc.perform(get("/api/contracts/99/detailed"))
                    .andExpect(status().isNotFound());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String validGenerateRequest() {
        return """
                {
                  "templateId": 1,
                  "userId": 1,
                  "clientId": 10,
                  "startDate": "2026-01-01",
                  "endDate": "2027-01-01",
                  "mappings": { "CompanyName": "Acme" },
                  "autoRenew": false,
                  "contractBalance": 5000.00
                }
                """;
    }
}

package clm.demo.controllers;

import clm.demo.exceptions.*;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * verifies that GlobalExceptionHandler maps each exception type to the correct HTTP status code
 * and returns a well-formed ErrorResponseDTO. uses a minimal probe controller that throws
 * exceptions on demand rather than wiring through a real service layer.
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new ProbeController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    // ------------------------------------------------------------------ //
    //  probe controller — each endpoint throws one specific exception      //
    // ------------------------------------------------------------------ //

    @RestController
    @RequestMapping("/probe")
    static class ProbeController {

        @GetMapping("/resource-not-found")
        void throwResourceNotFound() {
            throw new ResourceNotFoundException("item 99");
        }

        @GetMapping("/illegal-argument")
        void throwIllegalArgument() {
            throw new IllegalArgumentException("bad input provided");
        }

        @GetMapping("/duplicate-template")
        void throwDuplicateTemplate() {
            throw new DuplicateTemplateNameException("NDA already exists");
        }

        @GetMapping("/invalid-contract-state")
        void throwInvalidContractState() {
            throw new InvalidContractStateException("Contract is not ACTIVE");
        }

        @GetMapping("/invalid-appendix-state")
        void throwInvalidAppendixState() {
            throw new InvalidAppendixStateException("Appendix 12 is already SIGNED");
        }

        @GetMapping("/template-incomplete")
        void throwTemplateIncomplete() {
            throw new TemplateIncompleteException("Template has unmapped fields");
        }

        @GetMapping("/unsupported-file")
        void throwUnsupportedFile() {
            throw new UnsupportedFileException("only PDF and DOCX are supported");
        }

        @GetMapping("/file-conversion")
        void throwFileConversion() {
            throw new FileConversionException("DOCX to PDF conversion failed");
        }

        @GetMapping("/template-upload")
        void throwTemplateUpload() {
            throw new TemplateUploadException("failed to parse placeholders");
        }

        @GetMapping("/template-download")
        void throwTemplateDownload() {
            throw new TemplateDownloadException("decompression error");
        }

        @GetMapping("/contract-generation-fail")
        void throwContractGenerationFail() {
            throw new ContractGenerationFailException("rendering failed");
        }

        @GetMapping("/missing-mandatory-field")
        void throwMissingMandatoryField() {
            throw new MissingMandatoryFieldException(
                    "Missing mandatory fields", List.of("Client Name", "Date"));
        }

        @GetMapping("/template-field-ownership")
        void throwTemplateFieldOwnership() {
            throw new TemplateFieldOwnershipException("field 10 belongs to template 2, not 1");
        }

        @GetMapping("/signed-doc-unavailable")
        void throwSignedDocUnavailable() {
            throw new SignedDocumentNotAvailableException("contract 88 has no signed document");
        }

        @GetMapping("/empty-file-name")
        void throwEmptyFileName() {
            throw new EmptyFileNameException("file name is blank");
        }

        @GetMapping("/constraint-violation")
        void throwConstraintViolation() {
            throw new ConstraintViolationException("validation failed", new HashSet<>());
        }

        @GetMapping("/database-validation")
        void throwDatabaseValidation() {
            throw new DatabaseValidationException("constraint failed", "chk_positive_value",
                    "contractValue must be positive");
        }

        @GetMapping("/database-validation-no-details")
        void throwDatabaseValidationNoDetails() {
            throw new DatabaseValidationException("constraint failed");
        }

        @GetMapping("/data-access")
        void throwDataAccess() {
            throw new EmptyResultDataAccessException("expected 1 row, got 0", 1);
        }

        @GetMapping("/invalid-data-access-unique")
        void throwInvalidDataAccessUnique() {
            throw new InvalidDataAccessResourceUsageException("UNIQUE constraint violation");
        }

        @GetMapping("/invalid-data-access-check")
        void throwInvalidDataAccessCheck() {
            throw new InvalidDataAccessResourceUsageException("CHECK constraint failed");
        }

        @GetMapping("/invalid-data-access-unknown")
        void throwInvalidDataAccessUnknown() {
            throw new InvalidDataAccessResourceUsageException("some unrecognised db error");
        }

        @GetMapping("/unexpected")
        void throwUnexpected() {
            throw new RuntimeException("unexpected internal error");
        }

        // -- validation-specific probes --

        static class SampleBody {
            @NotNull(message = "required must not be null")
            public String required;
        }

        @PostMapping("/validate-body")
        void validateBody(@Valid @RequestBody SampleBody body) {}

        @PostMapping(value = "/multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        void acceptMultipart(@RequestParam("requiredPart") MultipartFile file) {}
    }

    // ================================================================== //
    //  404 — not found                                                     //
    // ================================================================== //

    @Nested
    class ResourceNotFound {

        @Test
        void maps_to_404_with_error_body() throws Exception {
            mockMvc.perform(get("/probe/resource-not-found"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Resource not found"));
        }
    }

    // ================================================================== //
    //  400 — bad request (various sources)                                 //
    // ================================================================== //

    @Nested
    class IllegalArgument {

        @Test
        void maps_to_400_with_details() throws Exception {
            mockMvc.perform(get("/probe/illegal-argument"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.details").value("bad input provided"));
        }
    }

    @Nested
    class MethodArgumentNotValid {

        @Test
        void maps_to_400_with_field_error_message() throws Exception {
            // sending {} — required field is null → MethodArgumentNotValidException
            mockMvc.perform(post("/probe/validate-body")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"));
        }
    }

    @Nested
    class MissingMultipartPart {

        @Test
        void maps_to_400_when_required_part_is_absent() throws Exception {
            // calling multipart endpoint without the required file part
            mockMvc.perform(multipart("/probe/multipart"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class ConstraintViolation {

        @Test
        void maps_to_400() throws Exception {
            mockMvc.perform(get("/probe/constraint-violation"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"));
        }
    }

    @Nested
    class MissingMandatoryField {

        @Test
        void maps_to_400_with_field_names_in_details() throws Exception {
            mockMvc.perform(get("/probe/missing-mandatory-field"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details").value(containsString("Client Name")));
        }
    }

    @Nested
    class TemplateFieldOwnership {

        @Test
        void maps_to_400() throws Exception {
            mockMvc.perform(get("/probe/template-field-ownership"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class EmptyFileName {

        @Test
        void maps_to_400() throws Exception {
            mockMvc.perform(get("/probe/empty-file-name"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class DatabaseValidation {

        @Test
        void maps_to_400_with_details_when_present() throws Exception {
            mockMvc.perform(get("/probe/database-validation"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details").value("contractValue must be positive"));
        }

        @Test
        void maps_to_400_using_message_when_details_absent() throws Exception {
            mockMvc.perform(get("/probe/database-validation-no-details"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details").value("constraint failed"));
        }
    }

    @Nested
    class InvalidDataAccess {

        @Test
        void unique_constraint_resolves_to_friendly_message() throws Exception {
            mockMvc.perform(get("/probe/invalid-data-access-unique"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details").value(
                            "A record with this value already exists."));
        }

        @Test
        void check_constraint_resolves_to_friendly_message() throws Exception {
            mockMvc.perform(get("/probe/invalid-data-access-check"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details").value(
                            "Data violates validation constraints."));
        }

        @Test
        void unknown_pattern_returns_generic_message() throws Exception {
            mockMvc.perform(get("/probe/invalid-data-access-unknown"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details").value(
                            "A data validation error occurred. Please check your input data."));
        }
    }

    // ================================================================== //
    //  409 — conflict (state violations)                                   //
    // ================================================================== //

    @Nested
    class InvalidContractState {

        @Test
        void maps_to_409() throws Exception {
            mockMvc.perform(get("/probe/invalid-contract-state"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409));
        }
    }

    @Nested
    class InvalidAppendixState {

        @Test
        void maps_to_409() throws Exception {
            mockMvc.perform(get("/probe/invalid-appendix-state"))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    class DuplicateTemplate {

        @Test
        void maps_to_409_with_message() throws Exception {
            mockMvc.perform(get("/probe/duplicate-template"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Template name already exists"));
        }
    }

    @Nested
    class SignedDocUnavailable {

        @Test
        void maps_to_409() throws Exception {
            mockMvc.perform(get("/probe/signed-doc-unavailable"))
                    .andExpect(status().isConflict());
        }
    }

    // ================================================================== //
    //  415 — unsupported media type                                        //
    // ================================================================== //

    @Nested
    class UnsupportedFile {

        @Test
        void maps_to_415() throws Exception {
            mockMvc.perform(get("/probe/unsupported-file"))
                    .andExpect(status().isUnsupportedMediaType())
                    .andExpect(jsonPath("$.status").value(415));
        }
    }

    // ================================================================== //
    //  422 — unprocessable entity                                          //
    // ================================================================== //

    @Nested
    class TemplateIncomplete {

        @Test
        void maps_to_422() throws Exception {
            mockMvc.perform(get("/probe/template-incomplete"))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.status").value(422));
        }
    }

    // ================================================================== //
    //  500 — internal server error                                         //
    // ================================================================== //

    @Nested
    class FileConversionFails {

        @Test
        void maps_to_500() throws Exception {
            mockMvc.perform(get("/probe/file-conversion"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500));
        }
    }

    @Nested
    class TemplateUploadFails {

        @Test
        void maps_to_500() throws Exception {
            mockMvc.perform(get("/probe/template-upload"))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    class TemplateDownloadFails {

        @Test
        void maps_to_500() throws Exception {
            mockMvc.perform(get("/probe/template-download"))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    class ContractGenerationFails {

        @Test
        void maps_to_500() throws Exception {
            mockMvc.perform(get("/probe/contract-generation-fail"))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    class DataAccessFails {

        @Test
        void maps_to_500_with_generic_message() throws Exception {
            mockMvc.perform(get("/probe/data-access"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.details").value(
                            "A database error occurred. Please contact support."));
        }
    }

    @Nested
    class UnexpectedException {

        @Test
        void maps_to_500_and_hides_internal_details() throws Exception {
            mockMvc.perform(get("/probe/unexpected"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.details").value(
                            "An unexpected error occurred. Please contact support."));
        }
    }

    // ================================================================== //
    //  error response structure                                            //
    // ================================================================== //

    @Nested
    class ErrorResponseStructure {

        @Test
        void response_body_contains_all_required_fields() throws Exception {
            mockMvc.perform(get("/probe/resource-not-found"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").isNumber())
                    .andExpect(jsonPath("$.message").isString())
                    .andExpect(jsonPath("$.details").isString())
                    .andExpect(jsonPath("$.timestamp").isString());
        }
    }
}

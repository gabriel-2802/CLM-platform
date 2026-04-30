package clm.demo.support;

import clm.demo.dto.requests.ContractTerminationRequest;
import clm.demo.dto.requests.FieldMappingRequest;
import clm.demo.dto.requests.GenAppendixRequest;
import clm.demo.dto.requests.GenContractRequest;
import clm.demo.dto.requests.SearchRequest;
import clm.demo.dto.responses.AppendixResponseDTO;
import clm.demo.dto.responses.ContractResponseDTO;
import clm.demo.dto.responses.TemplateFieldResponseDTO;
import clm.demo.dto.responses.TemplateResponseDTO;
import clm.demo.dto.responses.TemplateUploadResponseDTO;
import clm.demo.models.Appendix;
import clm.demo.models.Contract;
import clm.demo.models.DocumentFieldValue;
import clm.demo.models.DocumentTemplate;
import clm.demo.models.TemplateField;
import clm.demo.models.enums.AppendixStatus;
import clm.demo.models.enums.ContractStatus;
import clm.demo.models.enums.DataType;
import clm.demo.models.enums.DocumentFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * centralized factory for test fixtures — avoids duplicating builder boilerplate across test classes.
 * all methods return fully populated objects unless the name contains "minimal" or "partial".
 */
public final class TestDataFactory {

    private TestDataFactory() {}

    // ------------------------------------------------------------------ //
    //  magic bytes                                                         //
    // ------------------------------------------------------------------ //

    /** minimal valid pdf magic bytes (just enough to pass format detection). */
    public static byte[] pdfMagicBytes() {
        return new byte[]{0x25, 0x50, 0x44, 0x46, 0x2D};  // %PDF-
    }

    /** minimal valid docx/zip magic bytes (just enough to pass format detection). */
    public static byte[] docxMagicBytes() {
        return new byte[]{0x50, 0x4B, 0x03, 0x04};  // PK..
    }

    /** bytes that don't match any known format. */
    public static byte[] unknownFormatBytes() {
        return new byte[]{0x00, 0x01, 0x02, 0x03};
    }

    // ------------------------------------------------------------------ //
    //  models                                                              //
    // ------------------------------------------------------------------ //

    public static DocumentTemplate template(Long id, String name) {
        TemplateField field = TemplateField.builder()
                .id(100L + id)
                .fieldLabel("Client Name")
                .dataType(DataType.STRING)
                .fieldPosition(0)
                .isRequired(true)
                .build();

        DocumentTemplate t = DocumentTemplate.builder()
                .templateName(name)
                .description("test template")
                .documentFormat(DocumentFormat.DOCX)
                .documentContent(new byte[]{1, 2, 3})
                .fieldCount(1)
                .isFullyMapped(true)
                .templateFields(new ArrayList<>(List.of(field)))
                .build();

        // set id via reflection would require Hibernate proxy tricks in unit tests;
        // instead callers can use the returned object and configure mocks to return it
        try {
            var f = t.getClass().getSuperclass().getDeclaredField("id");
            f.setAccessible(true);
        } catch (Exception ignored) {}

        field.setDocumentTemplate(t);
        return t;
    }

    public static DocumentTemplate templateWithId(Long id, String name) {
        DocumentTemplate t = template(id, name);
        setId(t, id);
        return t;
    }

    public static TemplateField field(Long id, Long templateId, String label, int position) {
        return TemplateField.builder()
                .id(id)
                .fieldLabel(label)
                .dataType(DataType.STRING)
                .fieldPosition(position)
                .isRequired(true)
                .build();
    }

    public static Contract contract(Long id, ContractStatus status) {
        Contract c = Contract.builder()
                .clientId(42)
                .contractStatus(status)
                .contractValue(BigDecimal.valueOf(10_000))
                .contractBalance(BigDecimal.valueOf(5_000))
                .contractStartDate(LocalDate.of(2026, 1, 1))
                .contractEndDate(LocalDate.of(2027, 1, 1))
                .build();
        setId(c, id);
        return c;
    }

    public static Appendix appendix(Long id, Contract contract, AppendixStatus status) {
        Appendix a = Appendix.builder()
                .contract(contract)
                .title("Exhibit A")
                .appendixStatus(status)
                .documentFormat(DocumentFormat.PDF)
                .build();
        setId(a, id);
        return a;
    }

    public static DocumentFieldValue fieldValue(Long id, String label, String value) {
        TemplateField tf = TemplateField.builder()
                .id(id + 100)
                .fieldLabel(label)
                .dataType(DataType.STRING)
                .isRequired(true)
                .build();
        return DocumentFieldValue.builder()
                .id(id)
                .templateField(tf)
                .fieldValue(value)
                .build();
    }

    // ------------------------------------------------------------------ //
    //  requests                                                            //
    // ------------------------------------------------------------------ //

    public static GenContractRequest genContractRequest() {
        return new GenContractRequest(
                1L,
                10L,
                "staff@company.com",
                42L,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2027, 1, 1),
                Map.of("Client Name", "Acme Corp"),
                true,
                BigDecimal.valueOf(5_000),
                BigDecimal.valueOf(10_000),
                "test notes"
        );
    }

    public static GenAppendixRequest genAppendixRequest() {
        return new GenAppendixRequest(
                1L, 1L, "Exhibit A", 10L, "staff@company.com", "notes",
                Map.of("Client Name", "Acme Corp")
        );
    }

    public static ContractTerminationRequest terminationRequest() {
        return new ContractTerminationRequest(LocalDate.of(2026, 6, 1), "early exit");
    }

    public static FieldMappingRequest fieldMappingRequest(Long templateId, Long fieldId, String label) {
        FieldMappingRequest.FieldMappingDefinition def = new FieldMappingRequest.FieldMappingDefinition();
        def.setFieldId(fieldId);
        def.setFieldLabel(label);
        def.setDataType("STRING");
        def.setRequired(true);
        def.setFormatPattern("");

        FieldMappingRequest req = new FieldMappingRequest();
        req.setTemplateId(templateId);
        req.setMappings(List.of(def));
        return req;
    }

    public static SearchRequest searchRequest() {
        return new SearchRequest(null, ContractStatus.ACTIVE, null, null, null, null, null, null, null, 0, 20);
    }

    // ------------------------------------------------------------------ //
    //  response DTOs                                                       //
    // ------------------------------------------------------------------ //

    public static TemplateUploadResponseDTO uploadResponse(Long id, String name) {
        return TemplateUploadResponseDTO.builder()
                .templateId(id)
                .templateName(name)
                .documentText("Hello {{101}} and {{102}}")
                .build();
    }

    public static TemplateResponseDTO templateResponse(Long id, String name) {
        return TemplateResponseDTO.builder()
                .templateId(id)
                .templateName(name)
                .description("description")
                .fieldCount(1)
                .fullyMapped(true)
                .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .fields(List.of())
                .build();
    }

    public static ContractResponseDTO contractResponse(Long id, String status) {
        return ContractResponseDTO.builder()
                .id(id)
                .clientId(42)
                .contractStatus(status)
                .generatedByMail("staff@company.com")
                .contractStartDate(LocalDate.of(2026, 1, 1))
                .contractEndDate(LocalDate.of(2027, 1, 1))
                .createdAt(LocalDateTime.of(2026, 1, 1, 12, 0))
                .build();
    }

    public static AppendixResponseDTO appendixResponse(Long id, String status) {
        return AppendixResponseDTO.builder()
                .id(id)
                .contractId(1L)
                .title("Exhibit A")
                .appendixStatus(status)
                .documentFormat("PDF")
                .createdAt(LocalDateTime.of(2026, 1, 1, 12, 0))
                .build();
    }

    public static TemplateFieldResponseDTO fieldResponse(Long id, String label) {
        return new TemplateFieldResponseDTO(
                TemplateField.builder()
                        .id(id)
                        .fieldLabel(label)
                        .dataType(DataType.STRING)
                        .fieldPosition(0)
                        .isRequired(true)
                        .build()
        );
    }

    // ------------------------------------------------------------------ //
    //  internal helpers                                                    //
    // ------------------------------------------------------------------ //

    /** sets the {@code id} field on a JPA entity via reflection (bypasses the lack of a setter). */
    public static void setId(Object entity, Long id) {
        try {
            // walk the class hierarchy to find the id field (may be in a superclass)
            Class<?> clazz = entity.getClass();
            while (clazz != null) {
                try {
                    var f = clazz.getDeclaredField("id");
                    f.setAccessible(true);
                    f.set(entity, id);
                    return;
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("could not set id on " + entity.getClass().getSimpleName(), e);
        }
    }
}

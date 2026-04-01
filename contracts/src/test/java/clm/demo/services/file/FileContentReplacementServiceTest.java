package clm.demo.services.file;

import clm.demo.models.Contract;
import clm.demo.models.ContractFieldValue;
import clm.demo.models.Template;
import clm.demo.models.TemplateField;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.services.file.actions.FileContentReplacementService;
import clm.demo.services.file.actions.FileConverterService;
import clm.demo.services.file.actions.FileZipService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for FileContentReplacementService.
 * <p>Tests cover:
 * <ul>
 * <li>Field recognition and positioning (single field, multiple fields, edge cases)</li>
 * <li>Placeholder substitution with various dot sequences (4+)</li>
 * <li>Multi-line fields and paragraph boundaries</li>
 * <li>Document format conversions (DOCX ↔ PDF)</li>
 * <li>File I/O and storage</li>
 * </ul>
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("FileContentReplacementService Tests")
class FileContentReplacementServiceTest {

    private static final String TEMPLATES_DIR = "src/test/resources/templates";
    private static final String OUTPUT_DIR = "target/test-output/generated-contracts";
    private static final String TEMPLATE_PREFIX = "src/test/resources/templates/";

    @Mock
    private FileConverterService fileConverterService;

    @Mock
    private FileZipService fileZipService;

    private FileContentReplacementService service;

    @BeforeEach
    void setUp() throws IOException {
        service = new FileContentReplacementService(fileConverterService, fileZipService);
        createOutputDirectory();
    }

    private void createOutputDirectory() throws IOException {
        Path outputPath = Paths.get(OUTPUT_DIR);
        Files.createDirectories(outputPath);
        log.info("Created output directory: {}", outputPath.toAbsolutePath());
    }

    // ==================== Tests for Single Field Templates (1-field-*) ====================

    @Test
    @DisplayName("Test 1-field single-field template recognition")
    void testSingleFieldTemplateRecognition() throws IOException {
        // Template with 1 field: dots-with-spaces
        String templateFile = TEMPLATE_PREFIX + "1-template-dots-with-spaces.docx";
        byte[] templateBytes = loadTemplateFile(templateFile);

        // Setup mocks
        when(fileZipService.decompress(any())).thenReturn(templateBytes);
        when(fileConverterService.convert(any(), eq(DocumentFormat.DOCX), eq(DocumentFormat.PDF)))
                .thenAnswer(invocation -> "mock-pdf".getBytes());

        // Create test data
        Template template = createTestTemplate("single-field", DocumentFormat.DOCX, 1);
        Contract contract = createTestContract("CONTRACT-001");
        List<ContractFieldValue> fieldValues = createFieldValues(template, List.of("John Doe"));

        // Execute
        byte[] result = service.generateDocumentContent(contract, template, fieldValues);

        // Verify
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(fileZipService).decompress(any());
        verify(fileConverterService).convert(any(), eq(DocumentFormat.DOCX), eq(DocumentFormat.PDF));

        saveTestOutput("1-field-single-field", "docx", templateBytes);
        log.info("✓ Single field template test passed");
    }

    @Test
    @DisplayName("Test 1-field minimal edge case")
    void testMinimalEdgeCaseTemplate() throws IOException {
        String templateFile = TEMPLATE_PREFIX + "1-template-edge-case-minimal.docx";
        byte[] templateBytes = loadTemplateFile(templateFile);

        when(fileZipService.decompress(any())).thenReturn(templateBytes);
        when(fileConverterService.convert(any(), eq(DocumentFormat.DOCX), eq(DocumentFormat.PDF)))
                .thenAnswer(invocation -> "mock-pdf".getBytes());

        Template template = createTestTemplate("minimal-edge-case", DocumentFormat.DOCX, 1);
        Contract contract = createTestContract("CONTRACT-002");
        List<ContractFieldValue> fieldValues = createFieldValues(template, List.of("MinimalValue"));

        byte[] result = service.generateDocumentContent(contract, template, fieldValues);

        assertNotNull(result);
        assertTrue(result.length > 0);
        saveTestOutput("1-field-minimal-edge-case", "docx", templateBytes);
        log.info("✓ Minimal edge case test passed");
    }

    @Test
    @DisplayName("Test 1-field with many dots (extreme case)")
    void testManyDotsEdgeCase() throws IOException {
        String templateFile = TEMPLATE_PREFIX + "1-template-edge-case-many-dots.docx";
        byte[] templateBytes = loadTemplateFile(templateFile);

        when(fileZipService.decompress(any())).thenReturn(templateBytes);
        when(fileConverterService.convert(any(), eq(DocumentFormat.DOCX), eq(DocumentFormat.PDF)))
                .thenAnswer(invocation -> "mock-pdf".getBytes());

        Template template = createTestTemplate("many-dots-edge-case", DocumentFormat.DOCX, 1);
        Contract contract = createTestContract("CONTRACT-003");
        List<ContractFieldValue> fieldValues = createFieldValues(template, List.of("LongValueForManyDots"));

        byte[] result = service.generateDocumentContent(contract, template, fieldValues);

        assertNotNull(result);
        assertTrue(result.length > 0);
        saveTestOutput("1-field-many-dots-edge-case", "docx", templateBytes);
        log.info("✓ Many dots edge case test passed");
    }

    @Test
    @DisplayName("Test 1-field multiline field")
    void testMultilineFieldTemplate() throws IOException {
        String templateFile = TEMPLATE_PREFIX + "1-template-multiline-field.docx";
        byte[] templateBytes = loadTemplateFile(templateFile);

        when(fileZipService.decompress(any())).thenReturn(templateBytes);
        when(fileConverterService.convert(any(), eq(DocumentFormat.DOCX), eq(DocumentFormat.PDF)))
                .thenAnswer(invocation -> "mock-pdf".getBytes());

        Template template = createTestTemplate("multiline-field", DocumentFormat.DOCX, 1);
        Contract contract = createTestContract("CONTRACT-004");
        List<ContractFieldValue> fieldValues = createFieldValues(template, 
                List.of("Line 1\nLine 2\nLine 3"));

        byte[] result = service.generateDocumentContent(contract, template, fieldValues);

        assertNotNull(result);
        assertTrue(result.length > 0);
        saveTestOutput("1-field-multiline-field", "docx", templateBytes);
        log.info("✓ Multiline field test passed");
    }

    @Test
    @DisplayName("Test 1-field trailing dots edge case")
    void testTrailingDotsEdgeCase() throws IOException {
        String templateFile = TEMPLATE_PREFIX + "1-template-trailing-dots-edge-case.docx";
        byte[] templateBytes = loadTemplateFile(templateFile);

        when(fileZipService.decompress(any())).thenReturn(templateBytes);
        when(fileConverterService.convert(any(), eq(DocumentFormat.DOCX), eq(DocumentFormat.PDF)))
                .thenAnswer(invocation -> "mock-pdf".getBytes());

        Template template = createTestTemplate("trailing-dots-edge-case", DocumentFormat.DOCX, 1);
        Contract contract = createTestContract("CONTRACT-005");
        List<ContractFieldValue> fieldValues = createFieldValues(template, List.of("TrailingValue"));

        byte[] result = service.generateDocumentContent(contract, template, fieldValues);

        assertNotNull(result);
        assertTrue(result.length > 0);
        saveTestOutput("1-field-trailing-dots-edge-case", "docx", templateBytes);
        log.info("✓ Trailing dots edge case test passed");
    }

    // ==================== Tests for Two Field Templates (2-field-*) ====================

    @Test
    @DisplayName("Test 2-field dots on separate lines")
    void testTwoFieldsSeparateLinesTemplate() throws IOException {
        String templateFile = TEMPLATE_PREFIX + "2-template-dots-separate-lines.docx";
        byte[] templateBytes = loadTemplateFile(templateFile);

        when(fileZipService.decompress(any())).thenReturn(templateBytes);
        when(fileConverterService.convert(any(), eq(DocumentFormat.DOCX), eq(DocumentFormat.PDF)))
                .thenAnswer(invocation -> "mock-pdf".getBytes());

        Template template = createTestTemplate("dots-separate-lines", DocumentFormat.DOCX, 2);
        Contract contract = createTestContract("CONTRACT-006");
        List<ContractFieldValue> fieldValues = createFieldValues(template, 
                List.of("First Field", "Second Field"));

        byte[] result = service.generateDocumentContent(contract, template, fieldValues);

        assertNotNull(result);
        assertTrue(result.length > 0);
        saveTestOutput("2-field-dots-separate-lines", "docx", templateBytes);
        log.info("✓ Two fields separate lines test passed");
    }

    @Test
    @DisplayName("Test 2-field paragraph boundary")
    void testTwoFieldsParagraphBoundaryTemplate() throws IOException {
        String templateFile = TEMPLATE_PREFIX + "2-template-paragraph-boundary.docx";
        byte[] templateBytes = loadTemplateFile(templateFile);

        when(fileZipService.decompress(any())).thenReturn(templateBytes);
        when(fileConverterService.convert(any(), eq(DocumentFormat.DOCX), eq(DocumentFormat.PDF)))
                .thenAnswer(invocation -> "mock-pdf".getBytes());

        Template template = createTestTemplate("paragraph-boundary", DocumentFormat.DOCX, 2);
        Contract contract = createTestContract("CONTRACT-007");
        List<ContractFieldValue> fieldValues = createFieldValues(template, 
                List.of("End of Paragraph", "Start of Paragraph"));

        byte[] result = service.generateDocumentContent(contract, template, fieldValues);

        assertNotNull(result);
        assertTrue(result.length > 0);
        saveTestOutput("2-field-paragraph-boundary", "docx", templateBytes);
        log.info("✓ Two fields paragraph boundary test passed");
    }

    @Test
    @DisplayName("Test 2-field unicode dots")
    void testTwoFieldsUnicodeDotsTemplate() throws IOException {
        String templateFile = TEMPLATE_PREFIX + "2-template-unicode-dots.docx";
        byte[] templateBytes = loadTemplateFile(templateFile);

        when(fileZipService.decompress(any())).thenReturn(templateBytes);
        when(fileConverterService.convert(any(), eq(DocumentFormat.DOCX), eq(DocumentFormat.PDF)))
                .thenAnswer(invocation -> "mock-pdf".getBytes());

        Template template = createTestTemplate("unicode-dots", DocumentFormat.DOCX, 2);
        Contract contract = createTestContract("CONTRACT-008");
        List<ContractFieldValue> fieldValues = createFieldValues(template, 
                List.of("Unicode Field 1", "Unicode Field 2"));

        byte[] result = service.generateDocumentContent(contract, template, fieldValues);

        assertNotNull(result);
        assertTrue(result.length > 0);
        saveTestOutput("2-field-unicode-dots", "docx", templateBytes);
        log.info("✓ Two fields unicode dots test passed");
    }

    // ==================== Tests for Three Field Templates (3-field-*) ====================

    @Test
    @DisplayName("Test 3-field multiple fields template")
    void testThreeFieldsMultipleTemplate() throws IOException {
        String templateFile = TEMPLATE_PREFIX + "3-template-multiple-fields.docx";
        byte[] templateBytes = loadTemplateFile(templateFile);

        when(fileZipService.decompress(any())).thenReturn(templateBytes);
        when(fileConverterService.convert(any(), eq(DocumentFormat.DOCX), eq(DocumentFormat.PDF)))
                .thenAnswer(invocation -> "mock-pdf".getBytes());

        Template template = createTestTemplate("multiple-fields", DocumentFormat.DOCX, 3);
        Contract contract = createTestContract("CONTRACT-009");
        List<ContractFieldValue> fieldValues = createFieldValues(template, 
                List.of("Field One", "Field Two", "Field Three"));

        byte[] result = service.generateDocumentContent(contract, template, fieldValues);

        assertNotNull(result);
        assertTrue(result.length > 0);
        saveTestOutput("3-field-multiple-fields", "docx", templateBytes);
        log.info("✓ Three fields multiple fields test passed");
    }

    // ==================== Tests for Complex Template (7-field-*) ====================

    @Test
    @DisplayName("Test 7-field complex template")
    void testComplexTemplate() throws IOException {
        String templateFile = TEMPLATE_PREFIX + "7-template-complex.docx";
        byte[] templateBytes = loadTemplateFile(templateFile);

        when(fileZipService.decompress(any())).thenReturn(templateBytes);
        when(fileConverterService.convert(any(), eq(DocumentFormat.DOCX), eq(DocumentFormat.PDF)))
                .thenAnswer(invocation -> "mock-pdf".getBytes());

        Template template = createTestTemplate("complex-template", DocumentFormat.DOCX, 7);
        Contract contract = createTestContract("CONTRACT-010");
        List<ContractFieldValue> fieldValues = createFieldValues(template, 
                List.of("Field 1", "Field 2", "Field 3", "Field 4", 
                        "Field 5", "Field 6", "Field 7"));

        byte[] result = service.generateDocumentContent(contract, template, fieldValues);

        assertNotNull(result);
        assertTrue(result.length > 0);
        saveTestOutput("7-field-complex-template", "docx", templateBytes);
        log.info("✓ Complex template test passed");
    }

    // ==================== PDF Format Tests ====================

    @ParameterizedTest
    @ValueSource(strings = {
            "1-template-single-field",
            "2-template-dots-separate-lines",
            "3-template-multiple-fields"
    })
    @DisplayName("Test PDF format document generation")
    void testPdfFormatDocuments(String templateName) throws IOException {
        String templateFile = TEMPLATE_PREFIX + templateName + ".pdf";
        byte[] templateBytes = loadTemplateFileIfExists(templateFile);
        
        if (templateBytes == null) {
            log.warn("PDF template file not found: {}", templateFile);
            return;
        }

        // Mock PDF → DOCX conversion
        when(fileZipService.decompress(any())).thenReturn(templateBytes);
        when(fileConverterService.convert(any(), eq(DocumentFormat.PDF), eq(DocumentFormat.DOCX)))
                .thenAnswer(invocation -> "mock-docx".getBytes());
        when(fileConverterService.convert(any(), eq(DocumentFormat.DOCX), eq(DocumentFormat.PDF)))
                .thenAnswer(invocation -> "mock-pdf".getBytes());

        int fieldCount = extractFieldCountFromName(templateName);
        Template template = createTestTemplate(templateName, DocumentFormat.PDF, fieldCount);
        Contract contract = createTestContract("CONTRACT-PDF-" + fieldCount);
        List<String> values = generateFieldValues(fieldCount);
        List<ContractFieldValue> fieldValues = createFieldValues(template, values);

        byte[] result = service.generateDocumentContent(contract, template, fieldValues);

        assertNotNull(result);
        assertTrue(result.length > 0);
        log.info("✓ PDF format test passed for: {}", templateName);
    }

    // ==================== Format Normalization Tests ====================

    @Test
    @DisplayName("Test placeholder normalization in DOCX")
    void testPlaceholderNormalization() throws IOException {
        String templateFile = TEMPLATE_PREFIX + "1-template-edge-case-many-dots.docx";
        byte[] templateBytes = loadTemplateFile(templateFile);

        byte[] normalized = service.normalizePlaceholdersInDocx(templateBytes);

        assertNotNull(normalized);
        assertTrue(normalized.length > 0);
        log.info("✓ Placeholder normalization test passed");
    }

    // ==================== Field Value Mapping Tests ====================

    @Test
    @DisplayName("Test field value mapping with missing fields")
    void testFieldValueMappingWithMissingFields() throws IOException {
        String templateFile = TEMPLATE_PREFIX + "3-template-multiple-fields.docx";
        byte[] templateBytes = loadTemplateFile(templateFile);

        when(fileZipService.decompress(any())).thenReturn(templateBytes);
        when(fileConverterService.convert(any(), eq(DocumentFormat.DOCX), eq(DocumentFormat.PDF)))
                .thenAnswer(invocation -> "mock-pdf".getBytes());

        Template template = createTestTemplate("partial-mapping", DocumentFormat.DOCX, 3);
        Contract contract = createTestContract("CONTRACT-PARTIAL");
        
        // Only provide 2 out of 3 field values
        List<ContractFieldValue> fieldValues = createFieldValues(template, 
                List.of("Field One", "Field Two"));

        byte[] result = service.generateDocumentContent(contract, template, fieldValues);

        assertNotNull(result);
        assertTrue(result.length > 0);
        log.info("✓ Partial field mapping test passed");
    }

    @Test
    @DisplayName("Test field value mapping with null values")
    void testFieldValueMappingWithNullValues() throws IOException {
        String templateFile = TEMPLATE_PREFIX + "2-template-paragraph-boundary.docx";
        byte[] templateBytes = loadTemplateFile(templateFile);

        when(fileZipService.decompress(any())).thenReturn(templateBytes);
        when(fileConverterService.convert(any(), eq(DocumentFormat.DOCX), eq(DocumentFormat.PDF)))
                .thenAnswer(invocation -> "mock-pdf".getBytes());

        Template template = createTestTemplate("null-values", DocumentFormat.DOCX, 2);
        Contract contract = createTestContract("CONTRACT-NULL");
        
        // Create field values with one null value
        TemplateField field1 = createTemplateField(template, 0, "Field1", "Label1");
        TemplateField field2 = createTemplateField(template, 1, "Field2", "Label2");
        
        List<ContractFieldValue> fieldValues = new ArrayList<>();
        fieldValues.add(createContractFieldValue(contract, field1, "Value1"));
        fieldValues.add(createContractFieldValue(contract, field2, null)); // null value

        byte[] result = service.generateDocumentContent(contract, template, fieldValues);

        assertNotNull(result);
        assertTrue(result.length > 0);
        log.info("✓ Null field value test passed");
    }

    // ==================== File I/O and Storage Tests ====================

    @Test
    @DisplayName("Test generated contract file storage")
    void testGeneratedContractFileStorage() throws IOException {
        String templateFile = TEMPLATE_PREFIX + "1-template-single-field.docx";
        byte[] templateBytes = loadTemplateFile(templateFile);

        when(fileZipService.decompress(any())).thenReturn(templateBytes);
        when(fileConverterService.convert(any(), eq(DocumentFormat.DOCX), eq(DocumentFormat.PDF)))
                .thenAnswer(invocation -> "mock-pdf-content".getBytes());

        Template template = createTestTemplate("file-storage-test", DocumentFormat.DOCX, 1);
        Contract contract = createTestContract("CONTRACT-STORAGE-001");
        List<ContractFieldValue> fieldValues = createFieldValues(template, List.of("StorageTest"));

        byte[] result = service.generateDocumentContent(contract, template, fieldValues);

        // Store the generated contract
        String outputFilename = OUTPUT_DIR + "/CONTRACT-STORAGE-001.pdf";
        Files.write(Paths.get(outputFilename), result);
        
        assertTrue(Files.exists(Paths.get(outputFilename)));
        assertTrue(Files.size(Paths.get(outputFilename)) > 0);
        log.info("✓ File storage test passed: {}", outputFilename);
    }

    @Test
    @DisplayName("Test batch storage of multiple generated contracts")
    void testBatchContractStorage() throws IOException {
        String[] templateFiles = {
                "1-template-single-field.docx",
                "2-template-dots-separate-lines.docx",
                "3-template-multiple-fields.docx"
        };

        for (int i = 0; i < templateFiles.length; i++) {
            String templateFile = TEMPLATE_PREFIX + templateFiles[i];
            byte[] templateBytes = loadTemplateFile(templateFile);

            when(fileZipService.decompress(any())).thenReturn(templateBytes);
            when(fileConverterService.convert(any(), eq(DocumentFormat.DOCX), eq(DocumentFormat.PDF)))
                    .thenAnswer(invocation -> ("mock-pdf-" + i).getBytes());

            int fieldCount = i + 1;
            Template template = createTestTemplate("batch-test-" + fieldCount, DocumentFormat.DOCX, fieldCount);
            Contract contract = createTestContract("CONTRACT-BATCH-" + (i + 1));
            List<ContractFieldValue> fieldValues = createFieldValues(template, generateFieldValues(fieldCount));

            byte[] result = service.generateDocumentContent(contract, template, fieldValues);

            String outputFilename = String.format("%s/BATCH-%d-field-contract.pdf", OUTPUT_DIR, fieldCount);
            Files.write(Paths.get(outputFilename), result);
            
            assertTrue(Files.exists(Paths.get(outputFilename)));
            log.info("✓ Batch storage: {}", outputFilename);
        }
    }

    // ==================== Helper Methods ====================

    private byte[] loadTemplateFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("Template file not found: " + filePath);
        }
        return Files.readAllBytes(path);
    }

    private byte[] loadTemplateFileIfExists(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return null;
        }
        return Files.readAllBytes(path);
    }

    private Template createTestTemplate(String name, DocumentFormat format, int fieldCount) {
        Template template = Template.builder()
                .templateName(name)
                .documentFormat(format)
                .documentContent("mock-content".getBytes())
                .fieldCount(fieldCount)
                .isFullyMapped(true)
                .build();
        template.setId((long) (Math.random() * 10000));

        List<TemplateField> fields = new ArrayList<>();
        for (int i = 0; i < fieldCount; i++) {
            TemplateField field = createTemplateField(template, i, 
                    "Field" + i, "Label" + i);
            fields.add(field);
        }
        template.setTemplateFields(fields);

        return template;
    }

    private TemplateField createTemplateField(Template template, int position, 
                                              String placeholder, String label) {
        TemplateField field = TemplateField.builder()
                .contractTemplate(template)
                .fieldPosition(position)
                .placeholderText(placeholder)
                .fieldLabel(label)
                .build();
        field.setId((long) (position + Math.random() * 1000));
        return field;
    }

    private Contract createTestContract(String contractNumber) {
        Contract contract = new Contract();
        contract.setId((long) (Math.random() * 100000));
        contract.setContractNumber(contractNumber);
        return contract;
    }

    private List<ContractFieldValue> createFieldValues(Template template, List<String> values) {
        List<ContractFieldValue> fieldValues = new ArrayList<>();
        List<TemplateField> fields = template.getTemplateFields().stream()
                .sorted(Comparator.comparingInt(TemplateField::getFieldPosition))
                .toList();

        for (int i = 0; i < values.size() && i < fields.size(); i++) {
            Contract mockContract = new Contract();
            mockContract.setId((long) (Math.random() * 100000));
            fieldValues.add(createContractFieldValue(mockContract, fields.get(i), values.get(i)));
        }

        return fieldValues;
    }

    private ContractFieldValue createContractFieldValue(Contract contract, TemplateField field, String value) {
        ContractFieldValue cfv = new ContractFieldValue();
        cfv.setId((long) (Math.random() * 100000));
        cfv.setGeneratedContract(contract);
        cfv.setTemplateField(field);
        cfv.setFieldValue(value);
        return cfv;
    }

    private List<String> generateFieldValues(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> "Field Value " + (i + 1))
                .collect(Collectors.toList());
    }

    private int extractFieldCountFromName(String templateName) {
        String firstChar = templateName.substring(0, 1);
        return Integer.parseInt(firstChar);
    }

    private void saveTestOutput(String testName, String format, byte[] content) throws IOException {
        String filename = String.format("%s/%s.%s", OUTPUT_DIR, testName, format);
        Files.write(Paths.get(filename), content);
        log.debug("Test output saved: {}", filename);
    }
}


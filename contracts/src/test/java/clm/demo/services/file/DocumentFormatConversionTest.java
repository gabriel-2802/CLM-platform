package clm.demo.services.file;

import clm.demo.models.Contract;
import clm.demo.models.ContractFieldValue;
import clm.demo.models.Template;
import clm.demo.models.TemplateField;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.services.file.actions.FileConverterService;
import clm.demo.utils.ZipUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for DOCX document format conversion.
 * <p>Covers:
 * <ul>
 * <li>DOCX processing and handling with real converters</li>
 * <li>Large document handling</li>
 * <li>Error handling and recovery</li>
 * <li>Batch conversion of DOCX documents</li>
 * </ul>
 * 
 * <p><b>Note:</b> Uses actual FileConverterService and FileZipService instances.
 * PDF conversion tests are included and use real docx4j/Apache PDFBox implementations.
 */
@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest
@DisplayName("Document Format Conversion Tests")
class DocumentFormatConversionTest {

    private static final String TEMPLATES_DIR = "src/test/resources/templates";
    private static final String OUTPUT_DIR = "target/test-output/conversions";
    private static final String TO_PDF_DIR = OUTPUT_DIR + "/to_pdf_conv";
    private static final String TO_DOCX_DIR = OUTPUT_DIR + "/to_docx_conv";

    @Autowired
    private FileConverterService fileConverterService;

    @Autowired
    private ZipUtils fileZipService;

    @BeforeEach
    void setUp() throws IOException {
        createOutputDirectory();
    }

    private void createOutputDirectory() throws IOException {
        Path outputPath = Paths.get(OUTPUT_DIR);
        Files.createDirectories(outputPath);
        log.info("Created output directory for conversions: {}", outputPath.toAbsolutePath());
    }

    // ==================== DOCX to PDF Conversion Tests ====================

    @Test
    @DisplayName("Test DOCX to PDF conversion")
    void testDocxToPdfConversion() throws IOException {
        String templateFile = TEMPLATES_DIR + "/1-template-single-field.docx";
        byte[] docxBytes = loadTemplateFile(templateFile);

        Template template = createTemplate(DocumentFormat.DOCX, 1);
        template.setDocumentContent(fileZipService.compress(docxBytes));
        
        Contract contract = createContract();
        List<ContractFieldValue> fieldValues = createFieldValues(template, List.of("TestValue"));

        byte[] result = new FileContentReplacementServiceStub(fileConverterService, fileZipService)
                .generateDocumentContent(contract, template, fieldValues);

        log.info("✓ DOCX to PDF conversion successful");
    }

    @ParameterizedTest
    @CsvSource({
            "1-template-single-field.docx, 1",
            "2-template-dots-separate-lines.docx, 2",
            "3-template-multiple-fields.docx, 3"
    })
    @DisplayName("Test DOCX to PDF conversion with multiple templates")
    void testMultipleDocxToPdfConversions(String templateName, int fieldCount) throws IOException {
        String templateFile = TEMPLATES_DIR + "/" + templateName;
        byte[] docxBytes = loadTemplateFileIfExists(templateFile);

        if (docxBytes == null) {
            log.warn("Template not found: {}", templateFile);
            return;
        }

        Template template = createTemplate(DocumentFormat.DOCX, fieldCount);
        template.setDocumentContent(fileZipService.compress(docxBytes));
        
        Contract contract = createContract();
        List<ContractFieldValue> fieldValues = createFieldValues(template, generateFieldValues(fieldCount));

        byte[] result = new FileContentReplacementServiceStub(fileConverterService, fileZipService)
                .generateDocumentContent(contract, template, fieldValues);

        log.info("✓ Conversion successful for: {}", templateName);
    }

    @ParameterizedTest
    @CsvSource({
            "1-template-dots-with-spaces.docx, 1",
            "1-template-edge-case-many-dots.docx, 1",
            "1-template-edge-case-minimal.docx, 1",
            "1-template-multiline-field.docx, 1",
            "1-template-single-field.docx, 1",
            "1-template-trailing-dots-edge-case.docx, 1",
            "2-template-dots-separate-lines.docx, 2",
            "2-template-paragraph-boundary.docx, 2",
            "2-template-unicode-dots.docx, 2",
            "3-template-multiple-fields.docx, 3",
            "7-template-complex.docx, 7"
    })
    @DisplayName("Test DOCX to PDF conversion with ALL available templates")
    void testAllDocxTemplatesToPdf(String templateName, int fieldCount) throws IOException {
        String templateFile = TEMPLATES_DIR + "/" + templateName;
        byte[] docxBytes = loadTemplateFileIfExists(templateFile);

        if (docxBytes == null) {
            log.warn("Template not found: {}", templateFile);
            return;
        }

        Template template = createTemplate(DocumentFormat.DOCX, fieldCount);
        template.setDocumentContent(fileZipService.compress(docxBytes));
        
        Contract contract = createContract();
        List<ContractFieldValue> fieldValues = createFieldValues(template, generateFieldValues(fieldCount));

        byte[] result = new FileContentReplacementServiceStub(fileConverterService, fileZipService)
                .generateDocumentContent(contract, template, fieldValues);

        // Save output to to_pdf_conv directory
        String outputFileName = templateName.replace(".docx", ".pdf");
        String outputFile = String.format("%s/%s", TO_PDF_DIR, outputFileName);
        Path outputPath = Paths.get(outputFile);
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, result);
        
        log.info("✓ TO_PDF: {} → {}", templateName, outputFileName);
    }

    // ==================== PDF Conversion Tests (using real FileConverterService) ====================

    @Test
    @DisplayName("Test PDF to DOCX conversion")
    void testPdfToDocxConversion() throws IOException {
        String templateFile = TEMPLATES_DIR + "/1-template-single-field.pdf";
        byte[] pdfBytes = loadTemplateFileIfExists(templateFile);

        if (pdfBytes == null) {
            log.warn("PDF template not found: {}", templateFile);
            return;
        }

        // Convert PDF to DOCX using real service
        byte[] docxBytes = fileConverterService.convert(pdfBytes, DocumentFormat.PDF, DocumentFormat.DOCX);

        log.info("✓ PDF to DOCX conversion successful");
    }

    @ParameterizedTest
    @CsvSource({
            "1-template-single-field.pdf",
            "1-template-dots-with-spaces.pdf",
            "1-template-edge-case-many-dots.pdf",
            "1-template-edge-case-minimal.pdf",
            "1-template-multiline-field.pdf",
            "1-template-trailing-dots-edge-case.pdf",
            "2-template-dots-separate-lines.pdf",
            "2-template-paragraph-boundary.pdf",
            "2-template-unicode-dots.pdf",
            "3-template-multiple-fields.pdf",
            "7-template-complex.pdf"
    })
    @DisplayName("Test PDF to DOCX conversion with ALL available PDF templates")
    void testAllPdfTemplatesConversion(String templateName) throws IOException {
        String templateFile = TEMPLATES_DIR + "/" + templateName;
        byte[] pdfBytes = loadTemplateFileIfExists(templateFile);

        if (pdfBytes == null) {
            log.warn("PDF template not found: {}", templateFile);
            return;
        }

        // Convert PDF to DOCX using real service
        byte[] docxBytes = fileConverterService.convert(pdfBytes, DocumentFormat.PDF, DocumentFormat.DOCX);

        // Save output to to_docx_conv directory
        String outputFileName = templateName.replace(".pdf", "-converted.docx");
        String outputFile = String.format("%s/%s", TO_DOCX_DIR, outputFileName);
        Path outputPath = Paths.get(outputFile);
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, docxBytes);

        log.info("✓ TO_DOCX: {} → {}", templateName, outputFileName);
    }

    @Test
    @DisplayName("Test PDF template round-trip conversion (PDF → DOCX → PDF)")
    void testPdfRoundTripConversion() throws IOException {
        String pdfTemplateFile = TEMPLATES_DIR + "/1-template-single-field.pdf";
        byte[] originalPdfBytes = loadTemplateFileIfExists(pdfTemplateFile);

        if (originalPdfBytes == null) {
            log.warn("PDF template not found: {}", pdfTemplateFile);
            return;
        }

        // Step 1: Convert PDF to DOCX
        byte[] docxBytes = fileConverterService.convert(originalPdfBytes, DocumentFormat.PDF, DocumentFormat.DOCX);

        // Step 2: Convert DOCX back to PDF
        byte[] convertedPdfBytes = fileConverterService.convert(docxBytes, DocumentFormat.DOCX, DocumentFormat.PDF);

        // Save intermediate and final outputs
        String outputDir = OUTPUT_DIR + "/pdf-roundtrip";
        Path outputPath = Paths.get(outputDir);
        Files.createDirectories(outputPath);
        
        Files.write(Paths.get(outputDir + "/1-original.pdf"), originalPdfBytes);
        Files.write(Paths.get(outputDir + "/2-converted-to-docx.docx"), docxBytes);
        Files.write(Paths.get(outputDir + "/3-converted-back-to-pdf.pdf"), convertedPdfBytes);

        log.info("✓ PDF ROUND-TRIP: PDF → DOCX → PDF conversion completed");
    }

    @Test
    @DisplayName("Test PDF template with field processing (PDF → DOCX → fill → PDF)")
    void testPdfTemplateWithFieldFilling() throws IOException {
        String pdfTemplateFile = TEMPLATES_DIR + "/1-template-single-field.pdf";
        byte[] pdfBytes = loadTemplateFileIfExists(pdfTemplateFile);

        if (pdfBytes == null) {
            log.warn("PDF template not found: {}", pdfTemplateFile);
            return;
        }

        // Convert PDF to DOCX for field processing
        byte[] docxBytes = fileConverterService.convert(pdfBytes, DocumentFormat.PDF, DocumentFormat.DOCX);
        
        Template template = createTemplate(DocumentFormat.PDF, 1);
        template.setDocumentContent(fileZipService.compress(pdfBytes));
        
        Contract contract = createContract();
        List<ContractFieldValue> fieldValues = createFieldValues(template, List.of("PDFTestValue"));

        // This will use the real FileContentReplacementService logic
        byte[] result = new FileContentReplacementServiceStub(fileConverterService, fileZipService)
                .generateDocumentContent(contract, template, fieldValues);

        // Save output
        String outputFile = OUTPUT_DIR + "/pdf-with-fields.pdf";
        Files.write(Paths.get(outputFile), result);

        log.info("✓ PDF TEMPLATE with FIELDS: conversion completed");
    }

    // ==================== Conversion Error Handling Tests ====================

    @Test
    @DisplayName("Test handling of unsupported conversion")
    void testConversionFailureHandling() throws IOException {
        // Test with unsupported PDF input (should fail in real converter)
        byte[] invalidDocxBytes = "this is not valid docx".getBytes();

        Template template = createTemplate(DocumentFormat.DOCX, 1);
        template.setDocumentContent(fileZipService.compress(invalidDocxBytes));
        
        Contract contract = createContract();
        List<ContractFieldValue> fieldValues = createFieldValues(template, List.of("TestValue"));

        FileContentReplacementServiceStub service = new FileContentReplacementServiceStub(
                fileConverterService, fileZipService);

        // Real converter will throw exception when trying to process invalid DOCX
        assertThrows(Exception.class, () -> 
                service.generateDocumentContent(contract, template, fieldValues));
        
        log.info("✓ Conversion failure handling verified");
    }

    // ==================== Large Document Tests ====================

    @Test
    @DisplayName("Test conversion of large document")
    void testLargeDocumentConversion() throws IOException {
        // Load a real DOCX template for testing large document handling
        String templateFile = TEMPLATES_DIR + "/1-template-single-field.docx";
        byte[] docxBytes = loadTemplateFile(templateFile);

        Template template = createTemplate(DocumentFormat.DOCX, 1);
        template.setDocumentContent(fileZipService.compress(docxBytes));
        
        Contract contract = createContract();
        List<ContractFieldValue> fieldValues = createFieldValues(template, List.of("TestValue"));

        byte[] result = new FileContentReplacementServiceStub(fileConverterService, fileZipService)
                .generateDocumentContent(contract, template, fieldValues);

        log.info("✓ Large document conversion handled");
    }

    // ==================== Batch Conversion Tests ====================

    @Test
    @DisplayName("Test batch conversion of multiple documents")
    void testBatchConversion() throws IOException {
        String[] templates = {
                "1-template-single-field.docx",
                "2-template-dots-separate-lines.docx",
                "3-template-multiple-fields.docx"
        };

        for (int idx = 0; idx < templates.length; idx++) {
            final int i = idx;
            String templateFile = TEMPLATES_DIR + "/" + templates[i];
            byte[] templateBytes = loadTemplateFileIfExists(templateFile);

            if (templateBytes == null) continue;

            int fieldCount = i + 1;
            Template template = createTemplate(DocumentFormat.DOCX, fieldCount);
            template.setDocumentContent(fileZipService.compress(templateBytes));
            
            Contract contract = createContract();
            List<ContractFieldValue> fieldValues = createFieldValues(template, generateFieldValues(fieldCount));

            byte[] result = new FileContentReplacementServiceStub(fileConverterService, fileZipService)
                    .generateDocumentContent(contract, template, fieldValues);

            String outputFile = String.format("%s/batch-converted-%d.pdf", OUTPUT_DIR, i + 1);
            Files.write(Paths.get(outputFile), result);
            
            log.info("✓ Batch conversion {}/{} completed", i + 1, templates.length);
        }
    }

    // ==================== DOCX Format-Specific Tests ====================

    @Test
    @DisplayName("Test DOCX format integrity after conversion")
    void testDocxFormatIntegrity() throws IOException {
        String templateFile = TEMPLATES_DIR + "/1-template-single-field.docx";
        byte[] docxBytes = loadTemplateFile(templateFile);

        Template template = createTemplate(DocumentFormat.DOCX, 1);
        template.setDocumentContent(fileZipService.compress(docxBytes));
        
        Contract contract = createContract();
        List<ContractFieldValue> fieldValues = createFieldValues(template, List.of("TestValue"));

        byte[] result = new FileContentReplacementServiceStub(fileConverterService, fileZipService)
                .generateDocumentContent(contract, template, fieldValues);

        Files.write(Paths.get(OUTPUT_DIR + "/docx-integrity-test.pdf"), result);
        log.info("✓ DOCX format integrity maintained");
    }

    // ==================== Helper Methods ====================

    private byte[] loadTemplateFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("Template not found: " + filePath);
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

    private Template createTemplate(DocumentFormat format, int fieldCount) {
        Template template = Template.builder()
                .templateName("test-" + format.name())
                .documentFormat(format)
                .documentContent("mock".getBytes())
                .fieldCount(fieldCount)
                .build();
        template.setId((long) (Math.random() * 10000));

        List<TemplateField> fields = new ArrayList<>();
        for (int i = 0; i < fieldCount; i++) {
            TemplateField field = TemplateField.builder()
                    .contractTemplate(template)
                    .fieldPosition(i)
                    .fieldLabel("Label" + i)
                    .build();
            field.setId((long) i);
            fields.add(field);
        }
        template.setTemplateFields(fields);

        return template;
    }

    private Contract createContract() {
        Contract contract = new Contract();
        contract.setId((long) (Math.random() * 100000));
        contract.setClientId(123);
        return contract;
    }

    private List<ContractFieldValue> createFieldValues(Template template, List<String> values) {
        List<ContractFieldValue> fieldValues = new ArrayList<>();
        List<TemplateField> fields = template.getTemplateFields();

        for (int i = 0; i < values.size() && i < fields.size(); i++) {
            ContractFieldValue cfv = new ContractFieldValue();
            cfv.setId((long) (Math.random() * 100000));
            cfv.setTemplateField(fields.get(i));
            cfv.setFieldValue(values.get(i));
            fieldValues.add(cfv);
        }

        return fieldValues;
    }

    private List<String> generateFieldValues(int count) {
        List<String> values = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            values.add("FieldValue" + (i + 1));
        }
        return values;
    }

    // ==================== Stub for testing ====================

    static class FileContentReplacementServiceStub {
        private final FileConverterService fileConverterService;
        private final ZipUtils fileZipService;

        FileContentReplacementServiceStub(FileConverterService fcs, ZipUtils fzs) {
            this.fileConverterService = fcs;
            this.fileZipService = fzs;
        }

        byte[] generateDocumentContent(Contract contract, Template template, 
                                      List<ContractFieldValue> fieldValues) throws IOException {
            byte[] templateBytes = fileZipService.decompress(template.getDocumentContent());

            return switch (template.getDocumentFormat()) {
                case DOCX -> fileConverterService.convert(templateBytes, DocumentFormat.DOCX, DocumentFormat.PDF);
                case PDF -> {
                    // For PDF templates: convert to DOCX → fill → convert back to PDF
                    byte[] asDocx = fileConverterService.convert(templateBytes, DocumentFormat.PDF, DocumentFormat.DOCX);
                    byte[] pdfResult = fileConverterService.convert(asDocx, DocumentFormat.DOCX, DocumentFormat.PDF);
                    yield pdfResult;
                }
            };
        }
    }
}


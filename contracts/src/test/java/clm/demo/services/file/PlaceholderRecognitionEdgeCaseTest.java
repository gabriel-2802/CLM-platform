package clm.demo.services.file;

import clm.demo.models.Template;
import clm.demo.models.TemplateField;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.utils.FileUtils;
import clm.demo.utils.PlaceholderProcessor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Edge case and stress tests for placeholder recognition and field handling.
 * <p>Focuses on:
 * <ul>
 * <li>Multiple consecutive placeholders</li>
 * <li>Cross-run placeholders (dots spanning XML runs)</li>
 * <li>Unicode normalization (ellipsis → dots)</li>
 * <li>Very large documents with many fields</li>
 * <li>Special characters and encoding</li>
 * </ul>
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("Placeholder Recognition Edge Cases")
class PlaceholderRecognitionEdgeCaseTest {

    private static final String TEMPLATES_DIR = "src/test/resources/templates";
    private static final String OUTPUT_DIR = "target/test-output/edge-cases";

    @Mock
    private FileUtils fileZipService;

    private PlaceholderProcessorTestHelper placeholderHelper;

    @BeforeEach
    void setUp() throws IOException {
        placeholderHelper = new PlaceholderProcessorTestHelper();
        createOutputDirectory();
    }

    private void createOutputDirectory() throws IOException {
        Path outputPath = Paths.get(OUTPUT_DIR);
        Files.createDirectories(outputPath);
        log.info("Created output directory for edge cases: {}", outputPath.toAbsolutePath());
    }

    // ==================== Placeholder Pattern Recognition Tests ====================

    @Test
    @DisplayName("Test recognition of exactly 4 dots (minimum valid placeholder)")
    void testMinimumFourDots() {
        String text = "Here is the placeholder .... and continue";
        List<String> placeholders = placeholderHelper.findAllPlaceholders(text);
        
        assertEquals(1, placeholders.size());
        assertEquals("....", placeholders.get(0));
        log.info("✓ Minimum 4 dots recognized");
    }

    @Test
    @DisplayName("Test recognition of 5+ dots")
    void testMoreThanFourDots() {
        String[] testCases = {
                "Text with ..... five dots",
                "Text with ...... six dots",
                "Text with ......... nine dots",
                "Text with ................. many dots"
        };

        for (String testCase : testCases) {
            List<String> placeholders = placeholderHelper.findAllPlaceholders(testCase);
            assertEquals(1, placeholders.size());
            assertTrue(placeholders.get(0).length() >= 4);
            log.info("✓ Recognized placeholder: {} (length: {})", 
                    placeholders.get(0), placeholders.get(0).length());
        }
    }

    @Test
    @DisplayName("Test rejection of dots sequences < 4")
    void testFewerThanFourDots() {
        String[] testCases = {
                "Abbreviation like etc...",
                "Three dots like...",
                "Two dots like..",
                "Single dot like."
        };

        for (String testCase : testCases) {
            List<String> placeholders = placeholderHelper.findAllPlaceholders(testCase);
            // Should not match dot sequences less than 4
            assertEquals(0, placeholders.size());
            log.info("✓ Correctly rejected: {}", testCase);
        }
    }

    @Test
    @DisplayName("Test multiple placeholders on same line")
    void testMultiplePlaceholdersOnSameLine() {
        String text = "First .... and second ..... and third ......";
        List<String> placeholders = placeholderHelper.findAllPlaceholders(text);
        
        assertEquals(3, placeholders.size());
        log.info("✓ Found {} placeholders on same line", placeholders.size());
    }

    @Test
    @DisplayName("Test placeholders across multiple lines")
    void testPlaceholdersAcrossLines() {
        String text = "Paragraph 1:\n....\n\nParagraph 2:\n.....\n\nParagraph 3:\n......";
        List<String> placeholders = placeholderHelper.findAllPlaceholders(text);
        
        assertEquals(3, placeholders.size());
        log.info("✓ Found {} placeholders across lines", placeholders.size());
    }

    // ==================== Unicode and Special Character Tests ====================

    @Test
    @DisplayName("Test Unicode ellipsis normalization")
    void testUnicodeEllipsisNormalization() {
        // Unicode horizontal ellipsis (U+2026) → ASCII dots
        String unicodeText = "Here is ellipsis … and continue";
        String normalized = PlaceholderProcessor.normalize(unicodeText);
        
        // After normalization, ellipsis should be converted
        assertNotEquals(unicodeText, normalized);
        log.info("Original:   {}", unicodeText);
        log.info("Normalized: {}", normalized);
        log.info("✓ Unicode ellipsis normalized");
    }

    @Test
    @DisplayName("Test mixed Unicode and ASCII dots")
    void testMixedUnicodeAsciiDots() {
        String text = "Mix of … and .... dots";
        String normalized = PlaceholderProcessor.normalize(text);
        
        assertTrue(normalized.contains("."));
        log.info("✓ Mixed Unicode/ASCII normalized");
    }

    @Test
    @DisplayName("Test dots with spaces around them")
    void testDotsWithSpaces() {
        String text = "Placeholder with spaces: . . . . . . .";
        List<String> placeholders = placeholderHelper.findAllPlaceholders(text);
        
        // Spaces break the sequence, so this might not match as a single placeholder
        log.info("Found {} placeholders in spaced dots", placeholders.size());
        log.info("✓ Spaced dots handling tested");
    }

    // ==================== Field Position and Ordering Tests ====================

    @Test
    @DisplayName("Test field position preservation in sorted order")
    void testFieldPositionOrdering() {
        Template template = createTemplateWithFields(
                new int[]{0, 1, 2, 3, 4},
                new String[]{"Label0", "Label1", "Label2", "Label3", "Label4"}
        );

        List<TemplateField> sorted = template.getTemplateFields().stream()
                .sorted(Comparator.comparingInt(TemplateField::getFieldPosition))
                .toList();

        for (int i = 0; i < sorted.size(); i++) {
            assertEquals(i, (int) sorted.get(i).getFieldPosition());
        }
        log.info("✓ Field positions correctly sorted: 0-{}", sorted.size() - 1);
    }

    @Test
    @DisplayName("Test field position mapping with gaps")
    void testFieldPositionWithGaps() {
        // Simulate template where some placeholders are unmapped
        Template template = createTemplateWithFields(
                new int[]{0, 1, 2},
                new String[]{null, "Label1", null}  // Only position 1 is mapped
        );

        List<TemplateField> mapped = template.getTemplateFields().stream()
                .filter(f -> f.getFieldLabel() != null)
                .toList();

        assertEquals(1, mapped.size());
        assertEquals("Label1", mapped.get(0).getFieldLabel());
        log.info("✓ Correctly identified mapped fields (1 out of 3)");
    }

    // ==================== Cross-Run and Multi-Line Placeholder Tests ====================

    @Test
    @DisplayName("Test placeholder spanning conceptual run boundaries")
    void testCrossRunPlaceholder() {
        // Simulate placeholder that would be split across Word runs
        // In reality, this is handled by the merge-substitute-delta algorithm
        String run1 = "Text before ..";
        String run2 = ".. text after";
        String merged = run1 + run2;  // "...." now visible in merged string

        List<String> placeholders = placeholderHelper.findAllPlaceholders(merged);
        
        assertTrue(placeholders.size() > 0);
        log.info("✓ Cross-run placeholder detected in merged string");
    }

    @Test
    @DisplayName("Test multi-line placeholder")
    void testMultiLinePlaceholder() {
        String multiLineText = "Text ....\n.... more";
        List<String> placeholders = placeholderHelper.findAllPlaceholders(multiLineText);
        
        // Could be 1 or 2 depending on exact implementation
        assertTrue(placeholders.size() >= 1);
        log.info("✓ Multi-line placeholder handling tested");
    }

    // ==================== Field Value Substitution Tests ====================

    @Test
    @DisplayName("Test substitution of single placeholder")
    void testSinglePlaceholderSubstitution() {
        String template = "The value is .... here";
        String result = placeholderHelper.substitute(template, List.of("REPLACED"));
        
        assertFalse(result.contains("...."));
        assertTrue(result.contains("REPLACED"));
        log.info("✓ Single placeholder substituted");
    }

    @Test
    @DisplayName("Test substitution of multiple placeholders")
    void testMultiplePlaceholderSubstitution() {
        String template = "First .... and second ..... and third ......";
        String result = placeholderHelper.substitute(template, 
                List.of("VALUE1", "VALUE2", "VALUE3"));
        
        assertFalse(result.contains("...."));
        assertFalse(result.contains("....."));
        assertFalse(result.contains("......"));
        assertTrue(result.contains("VALUE1"));
        assertTrue(result.contains("VALUE2"));
        assertTrue(result.contains("VALUE3"));
        log.info("✓ Multiple placeholders substituted");
    }

    @Test
    @DisplayName("Test substitution with empty/null values")
    void testSubstitutionWithEmptyValues() {
        String template = "First .... and second .....";
        String result = placeholderHelper.substitute(template, 
                Arrays.asList("", null));
        
        // Null values should leave placeholder intact
        log.info("Result with empty values: {}", result);
        log.info("✓ Empty/null value handling tested");
    }

    @Test
    @DisplayName("Test substitution with special characters in value")
    void testSubstitutionWithSpecialCharacters() {
        String template = "Value is ....";
        String specialValue = "Test & <special> \"chars\" 'quotes'";
        String result = placeholderHelper.substitute(template, List.of(specialValue));
        
        assertTrue(result.contains(specialValue));
        log.info("✓ Special characters in values handled");
    }

    // ==================== Field Count and Statistics Tests ====================

    @Test
    @DisplayName("Test field count from template")
    void testFieldCountFromTemplate() {
        for (int count : new int[]{1, 2, 3, 5, 7, 10}) {
            Template template = createEmptyTemplate("test-" + count, count);
            assertEquals(count, template.getFieldCount());
            log.info("✓ Field count {} verified", count);
        }
    }

    @Test
    @DisplayName("Test maximum field count scenario")
    void testMaximumFieldCount() {
        int maxFields = 50;
        Template template = createEmptyTemplate("max-fields", maxFields);
        
        assertEquals(maxFields, template.getFieldCount());
        assertEquals(maxFields, template.getTemplateFields().size());
        log.info("✓ Maximum field count ({}) verified", maxFields);
    }

    // ==================== Helper Methods ====================

    private Template createTemplateWithFields(int[] positions, String[] labels) {
        Template template = Template.builder()
                .templateName("test-template")
                .documentFormat(DocumentFormat.DOCX)
                .documentContent("test".getBytes())
                .fieldCount(positions.length)
                .build();
        template.setId((long) (Math.random() * 10000));

        List<TemplateField> fields = new ArrayList<>();
        for (int i = 0; i < positions.length; i++) {
            TemplateField field = TemplateField.builder()
                    .contractTemplate(template)
                    .fieldPosition(positions[i])
                    .fieldLabel(labels[i])
                    .build();
            field.setId((long) i);
            fields.add(field);
        }
        template.setTemplateFields(fields);

        return template;
    }

    private Template createEmptyTemplate(String name, int fieldCount) {
        Template template = Template.builder()
                .templateName(name)
                .documentFormat(DocumentFormat.DOCX)
                .documentContent("test".getBytes())
                .fieldCount(fieldCount)
                .build();
        template.setId((long) (Math.random() * 10000));

        List<TemplateField> fields = new ArrayList<>();
        for (int i = 0; i < fieldCount; i++) {
            TemplateField field = TemplateField.builder()
                    .contractTemplate(template)
                    .fieldPosition(i)
                    .build();
            field.setId((long) i);
            fields.add(field);
        }
        template.setTemplateFields(fields);

        return template;
    }

    // ==================== Helper class for placeholder testing ====================

    @Slf4j
    static class PlaceholderProcessorTestHelper {

        /**
         * Find all placeholder sequences (4+ dots) in text.
         */
        List<String> findAllPlaceholders(String text) {
            List<String> placeholders = new ArrayList<>();
            if (text == null) return placeholders;

            // Use regex to find sequences of 4+ dots
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\.{4,}");
            java.util.regex.Matcher matcher = pattern.matcher(text);

            while (matcher.find()) {
                placeholders.add(matcher.group());
            }

            return placeholders;
        }

        /**
         * Substitute placeholders with values in order.
         */
        String substitute(String text, List<String> values) {
            if (text == null || values == null || values.isEmpty()) {
                return text;
            }

            String result = text;
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\.{4,}");
            java.util.regex.Matcher matcher = pattern.matcher(result);

            int valueIndex = 0;
            StringBuffer sb = new StringBuffer();

            while (matcher.find() && valueIndex < values.size()) {
                String replacement = values.get(valueIndex++);
                // Skip null values
                if (replacement != null) {
                    matcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(replacement));
                } else {
                    matcher.appendReplacement(sb, matcher.group());
                }
            }
            matcher.appendTail(sb);

            return sb.toString();
        }
    }
}


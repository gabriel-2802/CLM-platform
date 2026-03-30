package clm.demo.services.file.actions;

import clm.demo.models.Contract;
import clm.demo.models.ContractFieldValue;
import clm.demo.models.Template;
import clm.demo.models.TemplateField;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.utils.PlaceHolderUtils;
import clm.demo.utils.PlaceHolderUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

/**
 * Generates contract documents by replacing dot-sequence placeholders with field values.
 *
 * <p>Placeholders are matched positionally: the Nth dot-sequence in the document
 * corresponds to the TemplateField with {@code fieldPosition == N} (0-based, sorted).
 * The field's label is used to look up the value from the mappings map.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileContentReplacementService {
    private final FileConverterService fileConverterService;
    private final FileZipService fileZipService;

    /**
     * Generates a PDF by replacing each dot-sequence placeholder in the template
     * with the corresponding field value from {@code fieldValues}.
     */
    public byte[] generateDocumentContent(
            Contract contract,
            Template template,
            List<ContractFieldValue> fieldValues) throws IOException {

        log.info("Generating PDF for contract {} from {} template",
                contract.getId(), template.getDocumentFormat());

        // Build label → value lookup
        Map<String, String> labelToValue = buildLabelValueMap(fieldValues);

        // Fields ordered by position — index N maps to the Nth placeholder
        List<TemplateField> ordered = template.getTemplateFields().stream()
                .filter(f -> f.getFieldPosition() != null && f.getFieldLabel() != null)
                .sorted(Comparator.comparingInt(TemplateField::getFieldPosition))
                .toList();

        byte[] templateBytes = fileZipService.decompress(template.getDocumentContent());

        byte[] pdf = switch (template.getDocumentFormat()) {
            case DOCX -> {
                byte[] filled = fillDocx(templateBytes, ordered, labelToValue);
                yield fileConverterService.convert(filled, DocumentFormat.DOCX, DocumentFormat.PDF);
            }
            case PDF -> {
                byte[] asDocx = fileConverterService.convert(templateBytes, DocumentFormat.PDF, DocumentFormat.DOCX);
                byte[] filled = fillDocx(asDocx, ordered, labelToValue);
                yield fileConverterService.convert(filled, DocumentFormat.DOCX, DocumentFormat.PDF);
            }
        };

        log.info("PDF generated for contract {} ({} bytes)", contract.getId(), pdf.length);
        return pdf;
    }

    // -------------------------------------------------------------------------
    // Private — DOCX filling
    // -------------------------------------------------------------------------

    /**
     * Fills a DOCX by replacing each dot-sequence placeholder positionally.
     * Uses {@link PlaceHolderUtils#replaceEach} so the Nth match gets the value
     * of the Nth ordered field (looked up by label).
     */
    private byte[] fillDocx(
            byte[] docxBytes,
            List<TemplateField> ordered,
            Map<String, String> labelToValue) throws IOException {

        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(docxBytes))) {
            int[] globalIndex = {0};

            fillParagraphs(doc.getParagraphs(), ordered, labelToValue, globalIndex);

            doc.getTables().forEach(table ->
                    table.getRows().forEach(row ->
                            row.getTableCells().forEach(cell ->
                                    fillParagraphs(cell.getParagraphs(), ordered, labelToValue, globalIndex))));

            doc.getHeaderList().forEach(h ->
                    fillParagraphs(h.getParagraphs(), ordered, labelToValue, globalIndex));
            doc.getFooterList().forEach(f ->
                    fillParagraphs(f.getParagraphs(), ordered, labelToValue, globalIndex));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.write(out);
            return out.toByteArray();
        }
    }

    private void fillParagraphs(
            List<XWPFParagraph> paragraphs,
            List<TemplateField> ordered,
            Map<String, String> labelToValue,
            int[] globalIndex) {

        for (XWPFParagraph paragraph : paragraphs) {
            if (globalIndex[0] >= ordered.size()) break;
            fillParagraph(paragraph, ordered, labelToValue, globalIndex);
        }
    }

    /**
     * Replaces dot-sequence placeholders in a single paragraph.
     *
     * <p>Concatenates all runs into one string, applies
     * {@link PlaceHolderUtils#replaceEach} using the global index to pick the
     * right field for each match, then writes the result back into the first run
     * (preserving its style) and clears the rest.</p>
     */
    private void fillParagraph(
            XWPFParagraph paragraph,
            List<TemplateField> ordered,
            Map<String, String> labelToValue,
            int[] globalIndex) {

        List<XWPFRun> runs = paragraph.getRuns();
        if (runs.isEmpty()) return;

        StringBuilder sb = new StringBuilder();
        for (XWPFRun run : runs) {
            if (run.getText(0) != null) sb.append(run.getText(0));
        }
        String original = sb.toString();

        String rewritten = PlaceHolderUtils.replaceEach(original, i -> {
            int absIndex = globalIndex[0] + i;
            if (absIndex >= ordered.size()) return null;
            String label = ordered.get(absIndex).getFieldLabel();
            return labelToValue.getOrDefault(label, null);
        });

        if (rewritten.equals(original)) return;

        // Count how many placeholders were consumed in this paragraph
        long consumed = PlaceHolderUtils.findPlaceholders(original).size();
        globalIndex[0] += (int) consumed;

        runs.get(0).setText(rewritten, 0);
        for (int i = 1; i < runs.size(); i++) runs.get(i).setText("", 0);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Map<String, String> buildLabelValueMap(List<ContractFieldValue> fieldValues) {
        Map<String, String> map = new HashMap<>(fieldValues.size() * 2);
        for (ContractFieldValue cfv : fieldValues) {
            TemplateField field = cfv.getTemplateField();
            if (field != null && field.getFieldLabel() != null && cfv.getFieldValue() != null) {
                map.put(field.getFieldLabel(), cfv.getFieldValue());
            }
        }
        return map;
    }
}
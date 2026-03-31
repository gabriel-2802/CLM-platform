package clm.demo.services.file.actions;

import clm.demo.models.Contract;
import clm.demo.models.ContractFieldValue;
import clm.demo.models.Template;
import clm.demo.models.TemplateField;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.utils.PlaceholderProcessor;
import clm.demo.utils.PlaceholderProcessor.SubstitutionResult;
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
 * corresponds to the {@link TemplateField} with {@code fieldPosition == N} (0-based, sorted).
 * The field's label is used to look up the value from the fieldValues list.</p>
 *
 * <p><strong>Note:</strong> PDF templates are round-tripped through DOCX for filling
 * (PDF → DOCX → fill → PDF). This is inherently lossy; complex PDF layouts may degrade.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileContentReplacementService {

    private final FileConverterService fileConverterService;
    private final FileZipService       fileZipService;

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    public byte[] generateDocumentContent(
            Contract contract,
            Template template,
            List<ContractFieldValue> fieldValues) throws IOException {

        log.info("Generating PDF for contract {} from {} template",
                contract.getId(), template.getDocumentFormat());

        Map<String, String> labelToValue = buildLabelValueMap(fieldValues);
        List<TemplateField> ordered      = sortedFields(template);
        byte[] templateBytes             = fileZipService.decompress(template.getDocumentContent());

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
    // DOCX filling
    // -------------------------------------------------------------------------

    private byte[] fillDocx(
            byte[] docxBytes,
            List<TemplateField> ordered,
            Map<String, String> labelToValue) throws IOException {

        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(docxBytes))) {

            int[] globalIndex = {0};

            visitParagraphs(doc.getParagraphs(),           ordered, labelToValue, globalIndex);
            visitTables    (doc.getTables(),               ordered, labelToValue, globalIndex);
            visitHeaders   (doc.getHeaderList(),           ordered, labelToValue, globalIndex);
            visitFooters   (doc.getFooterList(),           ordered, labelToValue, globalIndex);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.write(out);
            return out.toByteArray();
        }
    }

    private void visitParagraphs(
            List<XWPFParagraph> paragraphs,
            List<TemplateField> ordered,
            Map<String, String> labelToValue,
            int[] globalIndex) {

        for (XWPFParagraph p : paragraphs) {
            if (globalIndex[0] >= ordered.size()) return;
            fillParagraph(p, ordered, labelToValue, globalIndex);
        }
    }

    private void visitTables(
            List<XWPFTable> tables,
            List<TemplateField> ordered,
            Map<String, String> labelToValue,
            int[] globalIndex) {

        for (XWPFTable table : tables) {
            if (globalIndex[0] >= ordered.size()) return;
            for (XWPFTableRow row : table.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    visitParagraphs(cell.getParagraphs(), ordered, labelToValue, globalIndex);
                }
            }
        }
    }

    private void visitHeaders(
            List<XWPFHeader> headers,
            List<TemplateField> ordered,
            Map<String, String> labelToValue,
            int[] globalIndex) {

        for (XWPFHeader h : headers) {
            visitParagraphs(h.getParagraphs(), ordered, labelToValue, globalIndex);
        }
    }

    private void visitFooters(
            List<XWPFFooter> footers,
            List<TemplateField> ordered,
            Map<String, String> labelToValue,
            int[] globalIndex) {

        for (XWPFFooter f : footers) {
            visitParagraphs(f.getParagraphs(), ordered, labelToValue, globalIndex);
        }
    }

    /**
     * Fills placeholders in a single paragraph while preserving per-run formatting.
     *
     * <p>Strategy: iterate runs one-by-one. If a run contains a placeholder, replace it
     * in-place so that the run's own formatting (bold, font, size, etc.) is kept intact.
     * Only runs that actually change are touched.</p>
     */
    private void fillParagraph(
            XWPFParagraph paragraph,
            List<TemplateField> ordered,
            Map<String, String> labelToValue,
            int[] globalIndex) {

        List<XWPFRun> runs = paragraph.getRuns();
        if (runs.isEmpty()) return;

        for (XWPFRun run : runs) {
            if (globalIndex[0] >= ordered.size()) return;

            String text = run.getText(0);
            if (text == null || text.isEmpty()) continue;

            SubstitutionResult result = PlaceholderProcessor.substituteEach(text, i -> {
                int absIndex = globalIndex[0] + i;
                if (absIndex >= ordered.size()) return null;
                String label = ordered.get(absIndex).getFieldLabel();
                return labelToValue.get(label);   // null → keep original
            });

            if (result.filledCount() == 0) continue;

            run.setText(result.text(), 0);
            globalIndex[0] += result.filledCount();
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static List<TemplateField> sortedFields(Template template) {
        return template.getTemplateFields().stream()
                .filter(f -> f.getFieldPosition() != null && f.getFieldLabel() != null)
                .sorted(Comparator.comparingInt(TemplateField::getFieldPosition))
                .toList();
    }

    private static Map<String, String> buildLabelValueMap(List<ContractFieldValue> fieldValues) {
        Map<String, String> map = new HashMap<>(fieldValues.size() * 2);
        for (ContractFieldValue cfv : fieldValues) {
            TemplateField field = cfv.getTemplateField();
            // Null values are excluded intentionally: a missing value leaves the placeholder intact.
            if (field != null && field.getFieldLabel() != null && cfv.getFieldValue() != null) {
                map.put(field.getFieldLabel(), cfv.getFieldValue());
            }
        }
        return map;
    }
}
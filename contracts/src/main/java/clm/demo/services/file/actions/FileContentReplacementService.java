package clm.demo.services.file.actions;

import clm.demo.models.Contract;
import clm.demo.models.ContractFieldValue;
import clm.demo.models.Template;
import clm.demo.models.TemplateField;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.utils.PlaceholderProcessor;
import clm.demo.utils.PlaceholderProcessor.SubstitutionResultWithSpans;
import clm.demo.utils.PlaceholderProcessor.SubstitutionSpan;
import clm.demo.utils.FileUtils;
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
public class FileContentReplacementService {

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
        byte[] templateBytes             = FileUtils.decompress(template.getDocumentContent());

        byte[] pdf = switch (template.getDocumentFormat()) {
            case DOCX -> {
                byte[] filled = fillDocx(templateBytes, ordered, labelToValue);
                yield FileUtils.convert(filled, DocumentFormat.DOCX, DocumentFormat.PDF);
            }
            case PDF -> {
                byte[] asDocx = FileUtils.convert(templateBytes, DocumentFormat.PDF, DocumentFormat.DOCX);
                byte[] filled = fillDocx(asDocx, ordered, labelToValue);
                yield FileUtils.convert(filled, DocumentFormat.DOCX, DocumentFormat.PDF);
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

            visitParagraphs(doc.getParagraphs(), ordered, labelToValue, globalIndex);
            visitTables    (doc.getTables(),     ordered, labelToValue, globalIndex);
            visitHeaders   (doc.getHeaderList(), ordered, labelToValue, globalIndex);
            visitFooters   (doc.getFooterList(), ordered, labelToValue, globalIndex);

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
     * Fills placeholders in a single paragraph with full per-run formatting preservation.
     *
     * <h3>Why run-by-run matching fails</h3>
     * Word splits paragraph text into many XML {@code <w:r>} runs. A dot-sequence
     * placeholder is almost always fragmented across several consecutive runs, so no
     * single run contains the full pattern and the regex never matches on a single run.
     *
     * <h3>Algorithm — merge → substitute → delta-based writeback</h3>
     * <ol>
     *   <li><b>Merge:</b> concatenate every run's text into one string, recording each
     *       run's start offset in {@code runStarts[i]} and the total merged length.</li>
     *   <li><b>Early exits:</b> skip if merged text is empty or all fields are exhausted.</li>
     *   <li><b>Substitute:</b> {@link PlaceholderProcessor#substituteEachWithSpans}
     *       returns the rewritten string plus one {@link SubstitutionSpan} per placeholder,
     *       recording its {@code [originalStart, originalEnd)} and {@code replacementLen}
     *       — all in original-string coordinates.</li>
     *   <li><b>Early exit:</b> if nothing was filled, no DOM mutation occurs.</li>
     *   <li><b>Delta-based writeback:</b> for each run, its original range is
     *       {@code [runStarts[r], runStarts[r+1])}. We compute a cumulative character
     *       delta from all spans whose {@code originalStart} falls before the run's
     *       original end, and use that delta to map the run's original end to its
     *       rewritten end:
     *       <pre>
     *         rwEnd = origEnd + cumulativeDelta(origEnd)
     *       </pre>
     *       The run receives {@code rewritten[rwStart .. rwEnd]}.
     *
     *       <p>Cross-run placeholder handling: a placeholder that starts inside run
     *       {@code i} gets its full replacement text attributed to run {@code i} (the
     *       run containing its original start). Runs {@code i+1..i+k} that contained
     *       only the tail of the placeholder receive empty strings — their original
     *       characters are fully accounted for by the delta.</p>
     *   </li>
     * </ol>
     *
     * <h3>Multiple placeholders on the same line</h3>
     * Because the delta accumulates across all spans in original-string order, and each
     * run's slice is computed independently from {@code runStarts[r]}, multiple
     * replacements on the same line are handled correctly without any interaction between
     * them.
     *
     * <h3>Multi-line / cross-run placeholders</h3>
     * Word joins consecutive runs within a paragraph; the merged string has no artificial
     * newlines between runs. If a placeholder's dots span a run boundary, the single
     * regex match in {@code substituteEachWithSpans} captures the whole sequence, and the
     * delta writeback correctly empties the tail runs.
     */
    private void fillParagraph(
            XWPFParagraph paragraph,
            List<TemplateField> ordered,
            Map<String, String> labelToValue,
            int[] globalIndex) {

        List<XWPFRun> runs = paragraph.getRuns();
        if (runs.isEmpty()) return;

        // ── Step 1: normalize + merge ─────────────────────────────────────────
        // Each run's text is normalized (Unicode ellipsis → ASCII dots) BEFORE
        // building runStarts, so all offsets live in the same coordinate space.

        String[] normTexts = new String[runs.size()];
        int[]    runStarts = new int[runs.size() + 1];
        StringBuilder merged = new StringBuilder();

        for (int i = 0; i < runs.size(); i++) {
            runStarts[i] = merged.length();
            normTexts[i] = PlaceholderProcessor.normalize(runs.get(i).getText(0));
            merged.append(normTexts[i]);
        }
        runStarts[runs.size()] = merged.length();

        if (merged.isEmpty()) return;

        // ── Step 2: substitute ────────────────────────────────────────────────
        SubstitutionResultWithSpans result = PlaceholderProcessor.substituteEachWithSpans(
                merged.toString(),
                i -> {
                    int absIndex = globalIndex[0] + i;
                    if (absIndex >= ordered.size()) return null;
                    String label = ordered.get(absIndex).getFieldLabel();
                    return labelToValue.get(label);
                });

        if (!result.anyFilled()) return;

        // ── Step 3: safe slice writeback ──────────────────────────────────────
        //
        // We walk the rewritten string once, slicing it back into runs.
        //
        // Key insight: the rewritten string is a transformation of the normalized
        // merged string. Every character in the normalized merged string maps to
        // either itself (plain char, 1-to-1) or is part of a replaced span (the
        // whole span maps to the replacement text). We track:
        //
        //   origPos  — current position in the normalized merged string
        //   rwPos    — current position in the rewritten string
        //   spanIdx  — next unconsumed span
        //
        // For each run we consume exactly (runStarts[r+1] - runStarts[r]) original
        // characters, translating to a slice [rwStart, rwEnd) of the rewritten string.
        // When origPos hits a span start we jump both cursors over the entire span
        // atomically — this handles cross-run placeholders correctly: the run that
        // contains the span start receives the full replacement, and runs that only
        // contained the tail of the placeholder get empty strings.

        String             rewritten = result.text();
        List<SubstitutionSpan> spans = result.spans();

        int origPos = 0;
        int rwPos   = 0;
        int spanIdx = 0;

        for (int r = 0; r < runs.size(); r++) {
            int origRunEnd = runStarts[r + 1];  // exclusive, in normalized coords
            int rwStart    = rwPos;

            while (origPos < origRunEnd) {
                if (spanIdx < spans.size()
                        && origPos == spans.get(spanIdx).originalStart()) {
                    // At a span boundary: jump both cursors over the entire span.
                    // origPos may land past origRunEnd if the placeholder crosses
                    // a run boundary — that is intentional: subsequent runs that
                    // only held the placeholder tail will have origPos > their
                    // origRunEnd and their while-loop won't execute, giving them "".
                    SubstitutionSpan sp = spans.get(spanIdx++);
                    origPos = sp.originalEnd();
                    rwPos  += sp.replacementLen();
                } else {
                    // Plain character: 1-to-1
                    origPos++;
                    rwPos++;
                }
            }

            // Safety clamp: rwPos must never exceed rewritten.length().
            // This guards against any floating-point-style accumulation error.
            int rwEnd = Math.min(rwPos, rewritten.length());
            runs.get(r).setText(rewritten.substring(rwStart, rwEnd), 0);
        }

        globalIndex[0] += result.filledCount();
    }


    // -------------------------------------------------------------------------
    // Template normalization — called on upload to canonicalize placeholders
    // -------------------------------------------------------------------------

    /**
     * Rewrites every placeholder dot-sequence in the DOCX to exactly four dots ({@code ....}).
     *
     * <p>Storing a canonical, fixed-width placeholder means the regex in
     * {@link clm.demo.utils.Constants#PLACEHOLDER_PATTERN} always matches runs of the
     * same predictable length, which significantly reduces the span-delta arithmetic
     * during contract generation and eliminates edge-cases around very long dot sequences
     * fragmenting across many XML runs.</p>
     *
     * <p>Uses the same merge → substitute → delta-based writeback algorithm as
     * {@link #fillParagraph} so cross-run dot sequences are handled correctly.</p>
     *
     * @param docxBytes raw DOCX bytes (not compressed)
     * @return DOCX bytes with every placeholder replaced by {@code ....}
     * @throws IOException if the document cannot be parsed or written
     */
    public byte[] normalizePlaceholdersInDocx(byte[] docxBytes) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(docxBytes))) {

            normalizeParagraphList(doc.getParagraphs());

            for (XWPFTable table : doc.getTables())
                for (XWPFTableRow row : table.getRows())
                    for (XWPFTableCell cell : row.getTableCells())
                        normalizeParagraphList(cell.getParagraphs());

            for (XWPFHeader h : doc.getHeaderList()) normalizeParagraphList(h.getParagraphs());
            for (XWPFFooter f : doc.getFooterList()) normalizeParagraphList(f.getParagraphs());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.write(out);
            log.debug("Placeholder normalization complete ({} bytes)", out.size());
            return out.toByteArray();
        }
    }

    private void normalizeParagraphList(List<XWPFParagraph> paragraphs) {
        for (XWPFParagraph p : paragraphs) normalizeParagraph(p);
    }

    private void normalizeParagraph(XWPFParagraph paragraph) {
        List<XWPFRun> runs = paragraph.getRuns();
        if (runs.isEmpty()) return;

        String[] normTexts = new String[runs.size()];
        int[]    runStarts = new int[runs.size() + 1];
        StringBuilder merged = new StringBuilder();

        for (int i = 0; i < runs.size(); i++) {
            runStarts[i] = merged.length();
            normTexts[i] = PlaceholderProcessor.normalize(runs.get(i).getText(0));
            merged.append(normTexts[i]);
        }
        runStarts[runs.size()] = merged.length();

        if (merged.isEmpty()) return;

        // Replace every placeholder with exactly 4 dots.
        SubstitutionResultWithSpans result =
                PlaceholderProcessor.substituteEachWithSpans(merged.toString(), i -> "....");

        if (!result.anyFilled()) return;

        String rewritten = result.text();
        List<SubstitutionSpan> spans = result.spans();
        int origPos = 0, rwPos = 0, spanIdx = 0;

        for (int r = 0; r < runs.size(); r++) {
            int origRunEnd = runStarts[r + 1];
            int rwStart    = rwPos;

            while (origPos < origRunEnd) {
                if (spanIdx < spans.size()
                        && origPos == spans.get(spanIdx).originalStart()) {
                    SubstitutionSpan sp = spans.get(spanIdx++);
                    origPos = sp.originalEnd();
                    rwPos  += sp.replacementLen();
                } else {
                    origPos++;
                    rwPos++;
                }
            }

            int rwEnd = Math.min(rwPos, rewritten.length());
            runs.get(r).setText(rewritten.substring(rwStart, rwEnd), 0);
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
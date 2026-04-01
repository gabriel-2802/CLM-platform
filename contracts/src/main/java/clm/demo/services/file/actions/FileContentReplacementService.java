package clm.demo.services.file.actions;

import clm.demo.models.Contract;
import clm.demo.models.ContractFieldValue;
import clm.demo.models.Template;
import clm.demo.models.TemplateField;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.utils.DocxTraversal;
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
import java.util.concurrent.atomic.AtomicInteger;

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
            AtomicInteger globalIndex = new AtomicInteger(0);
            DocxTraversal.forEachParagraph(doc, p -> fillParagraph(p, ordered, labelToValue, globalIndex));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.write(out);
            return out.toByteArray();
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
     *   <li><b>Merge:</b> concatenate every run's normalized text into one string, recording
     *       each run's start offset in {@code runStarts[i]}.</li>
     *   <li><b>Early exits:</b> skip if all fields are exhausted or merged text is empty.</li>
     *   <li><b>Substitute:</b> {@link PlaceholderProcessor#substituteEachWithSpans}
     *       returns the rewritten string plus one {@link SubstitutionSpan} per placeholder,
     *       recording its {@code [originalStart, originalEnd)} and {@code replacementLen}
     *       — all in original-string coordinates.</li>
     *   <li><b>Early exit:</b> if nothing was filled, no DOM mutation occurs.</li>
     *   <li><b>Delta-based writeback:</b> delegated to {@link #writebackSpans}.
     *       Cross-run placeholders are handled correctly: the run containing the
     *       placeholder's start receives the full replacement; runs that held only
     *       the tail receive empty strings.</li>
     * </ol>
     */
    private void fillParagraph(
            XWPFParagraph paragraph,
            List<TemplateField> ordered,
            Map<String, String> labelToValue,
            AtomicInteger globalIndex) {

        if (globalIndex.get() >= ordered.size()) return;

        List<XWPFRun> runs = paragraph.getRuns();
        if (runs.isEmpty()) return;

        // ── Step 1: normalize + merge ─────────────────────────────────────────
        // Each run's text is normalized (Unicode ellipsis → ASCII dots) BEFORE
        // building runStarts, so all offsets live in the same coordinate space.
        int[]         runStarts = new int[runs.size() + 1];
        StringBuilder merged    = new StringBuilder();

        for (int i = 0; i < runs.size(); i++) {
            runStarts[i] = merged.length();
            merged.append(PlaceholderProcessor.normalize(runs.get(i).getText(0)));
        }
        runStarts[runs.size()] = merged.length();

        if (merged.isEmpty()) return;

        // ── Step 2: substitute ────────────────────────────────────────────────
        int base = globalIndex.get();
        SubstitutionResultWithSpans result = PlaceholderProcessor.substituteEachWithSpans(
                merged.toString(),
                i -> {
                    int absIndex = base + i;
                    if (absIndex >= ordered.size()) return null;
                    String label = ordered.get(absIndex).getFieldLabel();
                    return labelToValue.get(label);
                });

        if (!result.anyFilled()) return;

        // ── Step 3: delta-based writeback ─────────────────────────────────────
        writebackSpans(runs, runStarts, result.text(), result.spans());

        globalIndex.addAndGet(result.filledCount());
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
            DocxTraversal.forEachParagraph(doc, this::normalizeParagraph);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.write(out);
            log.debug("Placeholder normalization complete ({} bytes)", out.size());
            return out.toByteArray();
        }
    }

    private void normalizeParagraph(XWPFParagraph paragraph) {
        List<XWPFRun> runs = paragraph.getRuns();
        if (runs.isEmpty()) return;

        int[]         runStarts = new int[runs.size() + 1];
        StringBuilder merged    = new StringBuilder();

        for (int i = 0; i < runs.size(); i++) {
            runStarts[i] = merged.length();
            merged.append(PlaceholderProcessor.normalize(runs.get(i).getText(0)));
        }
        runStarts[runs.size()] = merged.length();

        if (merged.isEmpty()) return;

        SubstitutionResultWithSpans result =
                PlaceholderProcessor.substituteEachWithSpans(merged.toString(), i -> "....");

        if (!result.anyFilled()) return;

        writebackSpans(runs, runStarts, result.text(), result.spans());
    }

    // -------------------------------------------------------------------------
    // Shared writeback
    // -------------------------------------------------------------------------

    /**
     * Slices {@code rewritten} back into {@code runs} using the original run boundaries
     * ({@code runStarts}) and the substitution span offsets.
     *
     * <p>We walk the rewritten string once per run. For each run we consume exactly
     * {@code runStarts[r+1] - runStarts[r]} original characters, translating that range
     * to a slice {@code [rwStart, rwEnd)} of the rewritten string. When {@code origPos}
     * hits a span start, both cursors jump over the entire span atomically — this handles
     * cross-run placeholders: the run containing the span start gets the full replacement,
     * and runs that held only the tail get empty strings.</p>
     *
     * <p>Shared by {@link #fillParagraph} and {@link #normalizeParagraph} so any fix
     * applies to both code paths automatically.</p>
     */
    private static void writebackSpans(
            List<XWPFRun> runs, int[] runStarts,
            String rewritten, List<SubstitutionSpan> spans) {

        int origPos = 0;
        int rwPos   = 0;
        int spanIdx = 0;

        for (int r = 0; r < runs.size(); r++) {
            int origRunEnd = runStarts[r + 1];
            int rwStart    = rwPos;

            while (origPos < origRunEnd) {
                if (spanIdx < spans.size()
                        && origPos == spans.get(spanIdx).originalStart()) {
                    // At a span boundary: jump both cursors over the entire span atomically.
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
            int rwEnd = Math.min(rwPos, rewritten.length());
            runs.get(r).setText(rewritten.substring(rwStart, rwEnd), 0);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private List<TemplateField> sortedFields(Template template) {
        List<TemplateField> all = template.getTemplateFields();
        long dropped = all.stream()
                .filter(f -> f.getFieldPosition() == null || f.getFieldLabel() == null)
                .count();
        if (dropped > 0) {
            log.warn("Template {}: {} field(s) with null position or label dropped from substitution" +
                     " — placeholder positions may misalign", template.getId(), dropped);
        }
        return all.stream()
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

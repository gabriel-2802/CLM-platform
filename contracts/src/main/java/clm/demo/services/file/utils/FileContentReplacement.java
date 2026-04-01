package clm.demo.services.file.utils;

import clm.demo.models.ContractFieldValue;
import clm.demo.models.Template;
import clm.demo.models.TemplateField;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.utils.DocxUtils;
import clm.demo.utils.PlaceholderProcessor;
import clm.demo.utils.PlaceholderProcessor.SubstitutionResultWithSpans;
import clm.demo.utils.PlaceholderProcessor.SubstitutionSpan;
import clm.demo.utils.FileUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * generates contract documents by replacing dot-sequence placeholders with field values.
 * placeholders are matched positionally: the Nth dot-sequence in the document
 * corresponds to the {@link TemplateField} with {@code fieldPosition == N} (0-based, sorted).
 * the field's label is used to look up the value from the fieldValues list.
 *
 * <p><strong>precondition:</strong> the template's stored DOCX must have been normalized
 * via {@link #normalizePlaceholdersInDocx} at upload time. calling {@link #generateDocumentContent}
 * on a non-normalized template will silently misalign placeholders.</p>
 *
 * <p><strong>usage:</strong>
 * <ol>
 *   <li>at upload time: call {@link #normalizePlaceholdersInDocx} on the raw DOCX bytes
 *       before compressing and storing the template.</li>
 *   <li>at contract generation time: call {@link #generateDocumentContent} with the stored
 *       template and the list of field values — returns a filled PDF.</li>
 * </ol>
 * </p>
 */
@Slf4j
@UtilityClass
public class FileContentReplacement {

    /**
     * generates a filled PDF contract from a normalized DOCX template and its field values.
     *
     * <p><strong>caller contract:</strong>
     * <ul>
     *   <li>{@code template.getDocumentContent()} must be a compressed, normalized DOCX
     *       (produced by {@link #normalizePlaceholdersInDocx} at upload time).</li>
     *   <li>{@code fieldValues} must cover all labeled fields; unlabeled or null-value
     *       entries are silently skipped and their placeholders are left intact.</li>
     *   <li>field order is determined by {@link TemplateField#getFieldPosition()}, not
     *       by the order of {@code fieldValues}.</li>
     * </ul>
     * </p>
     *
     * @param template    the template entity with field definitions and compressed DOCX content
     * @param fieldValues list of field values to fill in the placeholders
     * @return PDF document bytes with all resolvable placeholders filled
     * @throws IOException if decompression, DOCX parsing, or PDF conversion fails
     */
    public static byte[] generateDocumentContent(Template template, List<ContractFieldValue> fieldValues) throws IOException {

        Map<String, String> labelToValue = buildLabelValueMap(fieldValues);
        List<TemplateField> ordered = sortedFields(template);
        byte[] templateBytes = FileUtils.decompress(template.getDocumentContent());

        byte[] filled = fillDocx(templateBytes, ordered, labelToValue);
        return FileUtils.convert(filled, DocumentFormat.DOCX, DocumentFormat.PDF);
    }

    /**
     * fills placeholders in a DOCX document with field values, preserving run-level formatting.
     *
     * <p><strong>note:</strong> paragraphs are visited in document order (body → tables →
     * headers → footers) via {@link DocxUtils#forEachParagraph}. this order must match
     * the traversal used during normalization and placeholder counting, or positional
     * alignment will break.</p>
     *
     * @param docxBytes    raw (non-compressed) DOCX bytes — must already be normalized
     * @param ordered      template fields sorted ascending by {@code fieldPosition}
     * @param labelToValue map from field label to replacement value
     * @return DOCX bytes with placeholders filled
     * @throws IOException if the document cannot be read or written
     */
    private byte[] fillDocx(byte[] docxBytes, List<TemplateField> ordered, Map<String, String> labelToValue) throws IOException {

        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(docxBytes))) {
            AtomicInteger globalIndex = new AtomicInteger(0);
            DocxUtils.forEachParagraph(doc, p -> fillParagraph(p, ordered, labelToValue, globalIndex));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.write(out);
            return out.toByteArray();
        }
    }

    /**
     * fills placeholders in a single paragraph with full per-run formatting preservation.
     *
     * <p>algorithm — merge → substitute → delta-based writeback:</p>
     * <ol>
     *   <li><b>merge:</b> concatenate every run's text into one string, recording each run's
     *       start offset in {@code runStarts[i]}. no normalization is applied here because
     *       the stored DOCX is guaranteed to be already normalized.</li>
     *   <li><b>early exits:</b> skip if all fields are exhausted or merged text is empty.</li>
     *   <li><b>substitute:</b> {@link PlaceholderProcessor#substituteEachWithSpans} returns
     *       the rewritten string plus one {@link SubstitutionSpan} per placeholder, recording
     *       its {@code [originalStart, originalEnd)} and {@code replacementLen} in
     *       original-string coordinates.</li>
     *   <li><b>early exit:</b> if nothing was filled, no DOM mutation occurs.</li>
     *   <li><b>writeback:</b> delegated to {@link #writebackSpans}. the run containing a
     *       placeholder's start receives the full replacement; runs that held only the tail
     *       receive empty strings.</li>
     * </ol>
     *
     * <p><strong>precondition:</strong> run texts must already be normalized (all placeholders
     * are exactly {@code ....}). do not call {@link PlaceholderProcessor#normalize} here —
     * normalization is the responsibility of {@link #normalizeParagraph} at upload time.</p>
     *
     * @param paragraph   the DOCX paragraph to process
     * @param ordered     template fields sorted ascending by {@code fieldPosition}
     * @param labelToValue map of field labels to replacement values
     * @param globalIndex shared counter tracking how many fields have been consumed so far
     */
    private void fillParagraph(
            XWPFParagraph paragraph,
            List<TemplateField> ordered,
            Map<String, String> labelToValue,
            AtomicInteger globalIndex) {

        if (globalIndex.get() >= ordered.size()) return;

        List<XWPFRun> runs = paragraph.getRuns();
        if (runs.isEmpty()) return;

        // step 1: merge, template is guaranteed normalized at rest
        int[] runStarts = new int[runs.size() + 1];
        StringBuilder merged = new StringBuilder();

        for (int i = 0; i < runs.size(); i++) {
            runStarts[i] = merged.length();
            merged.append(runs.get(i).getText(0));
        }
        runStarts[runs.size()] = merged.length();

        if (merged.isEmpty()) return;

        // step 2: substitute
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

        // step 3: delta-based writeback
        writebackSpans(runs, runStarts, result.text(), result.spans());

        globalIndex.addAndGet(result.filledCount());
    }

    /**
     * rewrites every placeholder dot-sequence in the DOCX to exactly four dots ({@code ....}).
     *
     * <p>storing a canonical fixed-width placeholder means the regex in
     * {@link clm.demo.utils.Constants#PLACEHOLDER_PATTERN} always matches runs of the same
     * predictable length, reducing span-delta arithmetic during contract generation and
     * eliminating edge cases from long dot sequences fragmenting across many XML runs.</p>
     *
     * <p><strong>when to call:</strong> exactly once per template, immediately after the raw
     * DOCX bytes are available and before compressing and persisting the template entity.
     * do not call this at contract generation time — it is an upload-time operation only.</p>
     *
     * <p><strong>idempotent:</strong> safe to call multiple times — {@code ....} → {@code ....}
     * is a no-op substitution and paragraphs with no placeholders are skipped entirely.</p>
     *
     * @param docxBytes raw (non-compressed) DOCX bytes from the uploaded file
     * @return DOCX bytes with every placeholder replaced by exactly {@code ....}
     * @throws IOException if the document cannot be parsed or written
     */
    public byte[] normalizePlaceholdersInDocx(byte[] docxBytes) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(docxBytes))) {
            DocxUtils.forEachParagraph(doc, FileContentReplacement::normalizeParagraph);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.write(out);
            log.debug("placeholder normalization complete ({} bytes)", out.size());
            return out.toByteArray();
        }
    }

    /**
     * normalizes all dot-sequence placeholders in a single paragraph to exactly four dots.
     *
     * <p>this is the only place {@link PlaceholderProcessor#normalize} is called on run text,
     * because this method operates on raw user-supplied content which may contain CRLF line
     * endings or unicode dot-like glyphs (e.g. {@code U+2026 …}, {@code U+22EF ⋯}).
     * {@link #fillParagraph} must never call {@code normalize()} — by that point the document
     * is already clean.</p>
     *
     * <p>uses the same merge → substitute → delta-based writeback algorithm as
     * {@link #fillParagraph} to correctly handle cross-run placeholders.</p>
     *
     * @param paragraph the DOCX paragraph to normalize — called on raw upload-time content
     */
    private void normalizeParagraph(XWPFParagraph paragraph) {
        List<XWPFRun> runs = paragraph.getRuns();
        if (runs.isEmpty()) return;

        int[]         runStarts = new int[runs.size() + 1];
        StringBuilder merged    = new StringBuilder();

        for (int i = 0; i < runs.size(); i++) {
            runStarts[i] = merged.length();
            // normalize() is called here because this runs on raw user input —
            // the only place in this class where untrusted text is processed
            merged.append(PlaceholderProcessor.normalize(runs.get(i).getText(0)));
        }
        runStarts[runs.size()] = merged.length();

        if (merged.isEmpty()) return;

        SubstitutionResultWithSpans result =
                PlaceholderProcessor.substituteEachWithSpans(merged.toString(), i -> "....");

        if (!result.anyFilled()) return;

        writebackSpans(runs, runStarts, result.text(), result.spans());
    }


    /**
     * slices {@code rewritten} back into {@code runs} using original run boundaries
     * ({@code runStarts}) and substitution span offsets.
     *
     * <p>walks the rewritten string once per run. for each run, exactly
     * {@code runStarts[r+1] - runStarts[r]} original characters are consumed and translated
     * to a slice {@code [rwStart, rwEnd)} of the rewritten string. when {@code origPos} hits
     * a span start, both cursors jump over the entire span atomically — the run containing
     * the span start receives the full replacement text, and runs that held only the
     * placeholder tail receive empty strings.</p>
     *
     * <p><strong>shared by {@link #fillParagraph} and {@link #normalizeParagraph}:</strong>
     * any bug fix or edge-case handling here applies to both code paths automatically.
     * do not duplicate this logic.</p>
     *
     * @param runs      list of DOCX runs to write back into
     * @param runStarts array of per-run start offsets in the original merged string (length = runs.size() + 1)
     * @param rewritten the fully substituted string
     * @param spans     one {@link SubstitutionSpan} per placeholder, in match order
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
                    // at a span boundary: jump both cursors over the entire span atomically
                    SubstitutionSpan sp = spans.get(spanIdx++);
                    origPos = sp.originalEnd();
                    rwPos  += sp.replacementLen();
                } else {
                    // plain character: 1-to-1
                    origPos++;
                    rwPos++;
                }
            }

            // safety clamp: rwPos must never exceed rewritten.length()
            int rwEnd = Math.min(rwPos, rewritten.length());
            runs.get(r).setText(rewritten.substring(rwStart, rwEnd), 0);
        }
    }

    /**
     * retrieves and sorts all valid template fields by field position.
     *
     * <p>fields with a null position or null label are excluded from substitution.
     * if any are dropped, a warning is logged because positional alignment between
     * placeholders and fields will be off — placeholder N in the document will receive
     * the value of field N-1 for every dropped field preceding it.</p>
     *
     * @param template the template to extract fields from
     * @return list of valid fields sorted ascending by {@code fieldPosition}
     */
    private List<TemplateField> sortedFields(Template template) {
        List<TemplateField> all = template.getTemplateFields();
        long dropped = all.stream()
                .filter(f -> f.getFieldPosition() == null || f.getFieldLabel() == null)
                .count();
        if (dropped > 0) {
            log.warn("template {}: {} field(s) with null position or label dropped from substitution" +
                    " — placeholder positions may misalign", template.getId(), dropped);
        }
        return all.stream()
                .filter(f -> f.getFieldPosition() != null && f.getFieldLabel() != null)
                .sorted(Comparator.comparingInt(TemplateField::getFieldPosition))
                .toList();
    }

    /**
     * builds a map from field labels to their string values.
     *
     * <p>entries where the field, field label, or field value is null are excluded.
     * a missing value is intentional: the corresponding placeholder is left intact in
     * the output document rather than being replaced with an empty string.</p>
     *
     * @param fieldValues list of contract field values
     * @return map from field label to field value, never null
     */
    private static Map<String, String> buildLabelValueMap(List<ContractFieldValue> fieldValues) {
        Map<String, String> map = new HashMap<>(fieldValues.size() * 2);
        for (ContractFieldValue cfv : fieldValues) {
            TemplateField field = cfv.getTemplateField();
            // null values excluded intentionally: a missing value leaves the placeholder intact
            if (field != null && field.getFieldLabel() != null && cfv.getFieldValue() != null) {
                map.put(field.getFieldLabel(), cfv.getFieldValue());
            }
        }
        return map;
    }
}
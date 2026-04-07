package clm.demo.utils.docx;

import clm.demo.models.TemplateField;
import clm.demo.utils.file.PlaceholderProcessor;
import clm.demo.utils.file.PlaceholderProcessor.SubstitutionResultWithSpans;
import lombok.experimental.UtilityClass;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fills placeholders in a DOCX document with field values, preserving run-level formatting.
 *
 * <p>placeholders are matched positionally: the Nth dot-sequence in the document
 * corresponds to the {@link TemplateField} with {@code fieldPosition == N} (0-based, sorted).
 * the field's label is used to look up the value from the labelToValue map.</p>
 *
 * <p><strong>precondition:</strong> the DOCX must have been normalized via
 * {@link DocxNormalizer#normalizePlaceholdersInDocx} at upload time.
 * calling this on a non-normalized document will silently misalign placeholders.</p>
 *
 * <p><strong>usage:</strong> at contract generation time, call {@link #fillDocx} with the
 * normalized DOCX bytes, the list of ordered template fields, and a map of label-to-value
 * substitutions. returns the filled DOCX bytes.</p>
 */
@UtilityClass
public class DocxFiller {

    /**
     * fills placeholders in a DOCX document with field values, preserving run-level formatting.
     *
     * <p>paragraphs are visited in document order (body → tables → headers → footers) via
     * {@link DocxUtils#forEachParagraph}. this order must match the traversal used during
     * normalization and placeholder counting, or positional alignment will break.</p>
     *
     * @param docxBytes    raw (non-compressed) DOCX bytes — must already be normalized
     * @param ordered      template fields sorted ascending by {@code fieldPosition}
     * @param labelToValue map from field label to replacement value
     * @return DOCX bytes with placeholders filled
     * @throws IOException if the document cannot be read or written
     */
    public static byte[] fillDocx(byte[] docxBytes, List<TemplateField> ordered, Map<String, String> labelToValue) throws IOException {
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
     *       start offset. no normalization is applied here because the stored DOCX is guaranteed
     *       to be already normalized.</li>
     *   <li><b>early exits:</b> skip if all fields are exhausted or merged text is empty.</li>
     *   <li><b>substitute:</b> {@link PlaceholderProcessor#substituteEachWithSpans} returns
     *       the rewritten string plus one {@link PlaceholderProcessor.SubstitutionSpan} per
     *       placeholder, recording its {@code [originalStart, originalEnd)} and
     *       {@code replacementLen} in original-string coordinates.</li>
     *   <li><b>early exit:</b> if nothing was filled, no DOM mutation occurs.</li>
     *   <li><b>writeback:</b> delegated to {@link DocxUtils#writebackSpans}. the run containing
     *       a placeholder's start receives the full replacement; runs that held only the tail
     *       receive empty strings.</li>
     * </ol>
     *
     * <p><strong>precondition:</strong> run texts must already be normalized (all placeholders
     * are exactly {@code ....}). do not call {@link PlaceholderProcessor#normalize} here —
     * normalization is the responsibility of upload-time code only.</p>
     *
     * @param paragraph   the DOCX paragraph to process
     * @param ordered     template fields sorted ascending by {@code fieldPosition}
     * @param labelToValue map of field labels to replacement values
     * @param globalIndex shared counter tracking how many fields have been consumed so far
     */
    private static void fillParagraph(
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
            String t = runs.get(i).getText(0);
            merged.append(t != null ? t : "");
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
        DocxUtils.writebackSpans(runs, runStarts, result.text(), result.spans());

        globalIndex.addAndGet(result.filledCount());
    }
}


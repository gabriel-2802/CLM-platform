package clm.demo.utils.docx;

import clm.demo.utils.file.PlaceholderProcessor;
import lombok.experimental.UtilityClass;
import org.apache.poi.xwpf.usermodel.*;

import java.util.List;
import java.util.function.Consumer;

/**
 * Single source-of-truth for DOCX paragraph traversal order and run writeback.
 *
 * <p>All code that reads or rewrites document content should call
 * {@link #forEachParagraph} so that adding a new content zone (e.g. footnotes)
 * requires only one change here rather than updates scattered across
 * multiple service classes.</p>
 *
 * <p>Canonical order: body → table cells → headers → footers.</p>
 */
@UtilityClass
public class DocxUtils {

    /**
     * Visits every paragraph in {@code doc} in canonical order and passes it to {@code consumer}.
     */
    public static void forEachParagraph(XWPFDocument doc, Consumer<XWPFParagraph> consumer) {
        doc.getParagraphs().forEach(consumer);

        for (XWPFTable table : doc.getTables())
            for (XWPFTableRow row : table.getRows())
                for (XWPFTableCell cell : row.getTableCells())
                    cell.getParagraphs().forEach(consumer);

        for (XWPFHeader h : doc.getHeaderList()) h.getParagraphs().forEach(consumer);
        for (XWPFFooter f : doc.getFooterList()) f.getParagraphs().forEach(consumer);
    }

    /**
     * Slices {@code rewritten} back into {@code runs} using original run boundaries
     * ({@code runStarts}) and substitution span offsets.
     *
     * @param runs      list of DOCX runs to write back into
     * @param runStarts array of per-run start offsets in the original merged string (length = runs.size() + 1)
     * @param rewritten the fully substituted string
     * @param spans     one {@link PlaceholderProcessor.SubstitutionSpan} per placeholder, in match order
     */
    public static void writebackSpans(
            List<XWPFRun> runs, int[] runStarts,
            String rewritten, List<PlaceholderProcessor.SubstitutionSpan> spans) {

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
                    PlaceholderProcessor.SubstitutionSpan sp = spans.get(spanIdx++);
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
}

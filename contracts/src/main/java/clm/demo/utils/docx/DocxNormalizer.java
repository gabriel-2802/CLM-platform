package clm.demo.utils.docx;

import clm.demo.utils.file.PlaceholderProcessor;
import clm.demo.utils.file.PlaceholderProcessor.SubstitutionResultWithSpans;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Normalizes all dot-sequence placeholders in a DOCX document to exactly four dots ({@code ....}).
 *
 * <p>Storing a canonical fixed-width placeholder means the regex in
 * {@link clm.demo.utils.Constants#PLACEHOLDER_PATTERN} always matches runs of the same
 * predictable length, reducing span-delta arithmetic during contract generation and
 * eliminating edge cases from long dot sequences fragmenting across many XML runs.</p>
 *
 * <p><strong>When to call:</strong> exactly once per template, immediately after the raw
 * DOCX bytes are available and before compressing and persisting the template entity.
 * This is an upload-time operation only — do not call at generation time.</p>
 *
 * <p><strong>Idempotent:</strong> safe to call multiple times — {@code ....} → {@code ....}
 * is a no-op substitution and paragraphs with no placeholders are skipped entirely.</p>
 */
@Slf4j
@UtilityClass
public class DocxNormalizer {

    /**
     * Rewrites every placeholder dot-sequence in the DOCX to exactly four dots ({@code ....}).
     *
     * @param docxBytes raw (non-compressed) DOCX bytes from the uploaded file
     * @return DOCX bytes with every placeholder replaced by exactly {@code ....}
     * @throws IOException if the document cannot be parsed or written
     */
    public static byte[] normalizePlaceholdersInDocx(byte[] docxBytes) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(docxBytes))) {
            DocxUtils.forEachParagraph(doc, DocxNormalizer::normalizeParagraph);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.write(out);
            log.debug("placeholder normalization complete ({} bytes)", out.size());
            return out.toByteArray();
        }
    }

    /**
     * Normalizes all dot-sequence placeholders in a single paragraph to exactly four dots.
     *
     * <p>Uses the merge → substitute → delta-based writeback algorithm (via
     * {@link DocxUtils#writebackSpans}) to correctly handle cross-run placeholders
     * — i.e. dot sequences that span multiple XML {@code <w:r>} runs.</p>
     *
     * <p>This is the only place {@link PlaceholderProcessor#normalize} is called on run
     * text, because this method operates on raw user-supplied content which may contain
     * CRLF line endings or unicode dot-like glyphs (e.g. {@code U+2026 …}).
     * Downstream fill code must never call {@code normalize()} — by that point the
     * document is already clean.</p>
     *
     * @param paragraph the DOCX paragraph to normalize
     */
    private static void normalizeParagraph(XWPFParagraph paragraph) {
        List<XWPFRun> runs = paragraph.getRuns();
        if (runs.isEmpty()) return;

        // Merge all runs after normalizing unicode glyphs, so the regex can
        // match dot sequences that span multiple XML runs.
        int[]         runStarts = new int[runs.size() + 1];
        StringBuilder merged    = new StringBuilder();

        for (int i = 0; i < runs.size(); i++) {
            runStarts[i] = merged.length();
            String text = runs.get(i).getText(0);
            merged.append(PlaceholderProcessor.normalize(text != null ? text : ""));
        }
        runStarts[runs.size()] = merged.length();

        if (merged.isEmpty()) return;

        SubstitutionResultWithSpans result =
                PlaceholderProcessor.substituteEachWithSpans(merged.toString(), i -> "....");

        if (!result.anyFilled()) return;

        DocxUtils.writebackSpans(runs, runStarts, result.text(), result.spans());
    }
}

package clm.demo.utils;

import lombok.experimental.UtilityClass;
import org.apache.poi.xwpf.usermodel.*;

import java.util.function.Consumer;

/**
 * Single source-of-truth for DOCX paragraph traversal order.
 *
 * <p>All code that reads or rewrites document content should call
 * {@link #forEachParagraph} so that adding a new content zone (e.g. footnotes)
 * requires only one change here rather than updates scattered across
 * {@code FileParserService}, {@code FileContentReplacementService}, etc.</p>
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
}

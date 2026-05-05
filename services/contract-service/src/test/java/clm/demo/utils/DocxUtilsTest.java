package clm.demo.utils;

import clm.demo.utils.docx.DocxUtils;
import clm.demo.utils.file.PlaceholderProcessor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * verifies DocxUtils.forEachParagraph traversal order and
 * writebackSpans correctness under various single/multi-run scenarios.
 */
class DocxUtilsTest {

    // ================================================================== //
    //  forEachParagraph                                                    //
    // ================================================================== //

    @Nested
    class ForEachParagraph {

        @Test
        void visits_body_paragraphs_in_order() throws IOException {
            try (XWPFDocument doc = new XWPFDocument()) {
                doc.createParagraph().createRun().setText("first");
                doc.createParagraph().createRun().setText("second");

                List<String> visited = new ArrayList<>();
                DocxUtils.forEachParagraph(doc, p -> visited.add(p.getText()));

                assertThat(visited).contains("first", "second");
                assertThat(visited.indexOf("first")).isLessThan(visited.indexOf("second"));
            }
        }

        @Test
        void visits_table_cell_paragraphs() throws IOException {
            try (XWPFDocument doc = new XWPFDocument()) {
                XWPFTable table = doc.createTable(1, 1);
                table.getRow(0).getCell(0).getParagraphs().getFirst()
                        .createRun().setText("cell-text");

                List<String> visited = new ArrayList<>();
                DocxUtils.forEachParagraph(doc, p -> visited.add(p.getText()));

                assertThat(visited).contains("cell-text");
            }
        }

        @Test
        void body_paragraphs_visited_before_table_cells() throws IOException {
            try (XWPFDocument doc = new XWPFDocument()) {
                doc.createParagraph().createRun().setText("body");
                doc.createTable(1, 1).getRow(0).getCell(0)
                        .getParagraphs().getFirst().createRun().setText("table");

                List<String> visited = new ArrayList<>();
                DocxUtils.forEachParagraph(doc, p -> visited.add(p.getText()));

                assertThat(visited.indexOf("body")).isLessThan(visited.indexOf("table"));
            }
        }

        @Test
        void consumer_receives_correct_number_of_paragraphs() throws IOException {
            try (XWPFDocument doc = new XWPFDocument()) {
                // create 3 body paragraphs
                for (int i = 0; i < 3; i++) {
                    doc.createParagraph().createRun().setText("para" + i);
                }

                List<String> visited = new ArrayList<>();
                DocxUtils.forEachParagraph(doc, p -> visited.add(p.getText()));

                // default doc may have an initial empty paragraph — verify at least our 3
                long nonEmpty = visited.stream().filter(s -> !s.isBlank()).count();
                assertThat(nonEmpty).isGreaterThanOrEqualTo(3);
            }
        }
    }

    // ================================================================== //
    //  writebackSpans                                                      //
    // ================================================================== //

    @Nested
    class WritebackSpans {

        @Test
        void no_spans_leaves_text_unchanged() throws IOException {
            try (XWPFDocument doc = new XWPFDocument()) {
                XWPFParagraph para = doc.createParagraph();
                para.createRun().setText("plain text", 0);

                List<XWPFRun> runs    = para.getRuns();
                int[]         starts  = {0, 10};
                String        rewrite = "plain text";

                DocxUtils.writebackSpans(runs, starts, rewrite, List.of());

                assertThat(runs.getFirst().getText(0)).isEqualTo("plain text");
            }
        }

        @Test
        void single_run_single_span_replaced_correctly() throws IOException {
            // "Hello ...." → "Hello World"
            // span covers [6,10) with replacement length 5
            try (XWPFDocument doc = new XWPFDocument()) {
                XWPFParagraph para = doc.createParagraph();
                para.createRun().setText("Hello ....", 0);   // 10 chars

                List<XWPFRun> runs    = para.getRuns();
                int[]         starts  = {0, 10};
                String        rewrite = "Hello World";

                PlaceholderProcessor.SubstitutionSpan span =
                        new PlaceholderProcessor.SubstitutionSpan(6, 10, 5, true);

                DocxUtils.writebackSpans(runs, starts, rewrite, List.of(span));

                assertThat(runs.getFirst().getText(0)).isEqualTo("Hello World");
            }
        }

        @Test
        void placeholder_spanning_two_runs_written_to_first_run() throws IOException {
            // run0 "A: ...." (7 chars), run1 " end" (4 chars)
            // span [3,7) → "X", rewritten = "A: X end"
            try (XWPFDocument doc = new XWPFDocument()) {
                XWPFParagraph para = doc.createParagraph();
                XWPFRun r1 = para.createRun();
                r1.setText("A: ....", 0);   // 7 chars
                XWPFRun r2 = para.createRun();
                r2.setText(" end", 0);       // 4 chars

                List<XWPFRun> runs    = para.getRuns();
                int[]         starts  = {0, 7, 11};
                String        rewrite = "A: X end";

                PlaceholderProcessor.SubstitutionSpan span =
                        new PlaceholderProcessor.SubstitutionSpan(3, 7, 1, true);

                DocxUtils.writebackSpans(runs, starts, rewrite, List.of(span));

                assertThat(runs.get(0).getText(0)).isEqualTo("A: X");
                assertThat(runs.get(1).getText(0)).isEqualTo(" end");
            }
        }

        @Test
        void unsubstituted_span_preserves_original_dots() throws IOException {
            // span with replaced=false: dots remain in output
            try (XWPFDocument doc = new XWPFDocument()) {
                XWPFParagraph para = doc.createParagraph();
                para.createRun().setText("Name: ....", 0);   // 10 chars

                List<XWPFRun> runs    = para.getRuns();
                int[]         starts  = {0, 10};
                String        rewrite = "Name: ....";    // unchanged — no replacement

                PlaceholderProcessor.SubstitutionSpan span =
                        new PlaceholderProcessor.SubstitutionSpan(6, 10, 4, false);

                DocxUtils.writebackSpans(runs, starts, rewrite, List.of(span));

                assertThat(runs.getFirst().getText(0)).isEqualTo("Name: ....");
            }
        }
    }
}

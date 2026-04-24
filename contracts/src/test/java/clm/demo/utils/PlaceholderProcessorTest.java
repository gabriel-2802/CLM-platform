package clm.demo.utils;

import clm.demo.utils.file.PlaceholderProcessor;
import clm.demo.utils.file.PlaceholderProcessor.PlaceholderRecord;
import clm.demo.utils.file.PlaceholderProcessor.SubstitutionResult;
import clm.demo.utils.file.PlaceholderProcessor.SubstitutionResultWithSpans;
import clm.demo.utils.file.PlaceholderProcessor.SubstitutionSpan;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlaceholderProcessorTest {

    // ================================================================== //
    //  normalize                                                           //
    // ================================================================== //

    @Nested
    class Normalize {

        @Test
        void null_returns_empty_string() {
            assertThat(PlaceholderProcessor.normalize(null)).isEmpty();
        }

        @Test
        void empty_string_returns_empty() {
            assertThat(PlaceholderProcessor.normalize("")).isEmpty();
        }

        @Test
        void plain_text_unchanged() {
            assertThat(PlaceholderProcessor.normalize("hello world")).isEqualTo("hello world");
        }

        @Test
        void crlf_replaced_with_lf() {
            assertThat(PlaceholderProcessor.normalize("line1\r\nline2")).isEqualTo("line1\nline2");
        }

        @Test
        void bare_cr_replaced_with_lf() {
            assertThat(PlaceholderProcessor.normalize("line1\rline2")).isEqualTo("line1\nline2");
        }

        @Test
        void horizontal_ellipsis_expanded_to_three_dots() {
            // U+2026 → "..."
            assertThat(PlaceholderProcessor.normalize("\u2026")).isEqualTo("...");
        }

        @Test
        void midline_ellipsis_expanded_to_three_dots() {
            // U+22EF → "..."
            assertThat(PlaceholderProcessor.normalize("\u22EF")).isEqualTo("...");
        }

        @Test
        void two_dot_leader_expanded_to_two_dots() {
            // U+2025 → ".."
            assertThat(PlaceholderProcessor.normalize("\u2025")).isEqualTo("..");
        }

        @Test
        void fullwidth_full_stop_expanded_to_one_dot() {
            // U+FF0E → "."
            assertThat(PlaceholderProcessor.normalize("\uFF0E")).isEqualTo(".");
        }

        @Test
        void mixed_unicode_glyphs_all_expanded() {
            // two horizontal ellipses = "......" (6 dots) — qualifies as a placeholder
            String input = "\u2026\u2026";
            assertThat(PlaceholderProcessor.normalize(input)).isEqualTo("......");
        }

        @Test
        void multiple_normalizations_applied_together() {
            String input = "name:\r\n\uFF0E\u2026\u2025\u22EF";
            // \r\n→\n, \uFF0E→"." (1), \u2026→"..." (3), \u2025→".." (2), \u22EF→"..." (3) = 9 dots
            assertThat(PlaceholderProcessor.normalize(input)).isEqualTo("name:\n" + ".".repeat(9));
        }
    }

    // ================================================================== //
    //  findPlaceholders                                                    //
    // ================================================================== //

    @Nested
    class FindPlaceholders {

        @Test
        void no_placeholders_returns_empty_list() {
            List<PlaceholderRecord> result = PlaceholderProcessor.findPlaceholders("no dots here");
            assertThat(result).isEmpty();
        }

        @Test
        void fewer_than_four_dots_not_matched() {
            assertThat(PlaceholderProcessor.findPlaceholders("...")).isEmpty();
        }

        @ParameterizedTest(name = "{0} dots → 1 placeholder")
        @ValueSource(strings = {"....", ".....", "......", "........."})
        void four_or_more_consecutive_dots_matched(String dots) {
            List<PlaceholderRecord> result = PlaceholderProcessor.findPlaceholders(dots);
            assertThat(result).hasSize(1);
        }

        @Test
        void single_placeholder_metadata_correct() {
            List<PlaceholderRecord> result = PlaceholderProcessor.findPlaceholders("Name: ....");
            assertThat(result).hasSize(1);

            PlaceholderRecord r = result.getFirst();
            assertThat(r.occurrenceIndex()).isZero();
            assertThat(r.prevText()).isEqualTo("....");
            assertThat(r.startOffset()).isEqualTo(6);
            assertThat(r.endOffset()).isEqualTo(10);
        }

        @Test
        void multiple_placeholders_indexed_sequentially() {
            String text = "A: .... B: ..... C: ......";
            List<PlaceholderRecord> result = PlaceholderProcessor.findPlaceholders(text);
            assertThat(result).hasSize(3);
            assertThat(result.get(0).occurrenceIndex()).isZero();
            assertThat(result.get(1).occurrenceIndex()).isEqualTo(1);
            assertThat(result.get(2).occurrenceIndex()).isEqualTo(2);
        }

        @Test
        void dots_with_intermediate_spaces_counted_as_one_placeholder() {
            // pattern allows optional horizontal whitespace between dot groups
            List<PlaceholderRecord> result = PlaceholderProcessor.findPlaceholders(".... ....");
            // depends on pattern: \\.{4,}(?:[ \\t]*\\.+)* — this matches as one
            assertThat(result).hasSize(1);
        }

        @Test
        void placeholder_surrounded_by_text() {
            List<PlaceholderRecord> result = PlaceholderProcessor.findPlaceholders("Hello .... World");
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().startOffset()).isEqualTo(6);
            assertThat(result.getFirst().endOffset()).isEqualTo(10);
        }
    }

    // ================================================================== //
    //  substituteEach                                                      //
    // ================================================================== //

    @Nested
    class SubstituteEach {

        @Test
        void no_placeholders_returns_text_unchanged() {
            SubstitutionResult result = PlaceholderProcessor.substituteEach("no dots", i -> "VALUE");
            assertThat(result.text()).isEqualTo("no dots");
            assertThat(result.filledCount()).isZero();
        }

        @Test
        void single_placeholder_replaced() {
            SubstitutionResult result = PlaceholderProcessor.substituteEach("Name: ....", i -> "Alice");
            assertThat(result.text()).isEqualTo("Name: Alice");
            assertThat(result.filledCount()).isEqualTo(1);
        }

        @Test
        void multiple_placeholders_all_replaced() {
            SubstitutionResult result = PlaceholderProcessor.substituteEach(
                    "A: .... B: .....",
                    i -> switch (i) {
                        case 0 -> "first";
                        case 1 -> "second";
                        default -> null;
                    });
            assertThat(result.text()).isEqualTo("A: first B: second");
            assertThat(result.filledCount()).isEqualTo(2);
        }

        @Test
        void null_resolver_result_keeps_original_dots() {
            SubstitutionResult result = PlaceholderProcessor.substituteEach("Name: ....", i -> null);
            assertThat(result.text()).isEqualTo("Name: ....");
            assertThat(result.filledCount()).isZero();
        }

        @Test
        void partial_replacement_only_counts_non_null() {
            // 2 placeholders, only index 0 replaced
            SubstitutionResult result = PlaceholderProcessor.substituteEach(
                    "A: .... B: .....",
                    i -> i == 0 ? "VALUE" : null);
            assertThat(result.filledCount()).isEqualTo(1);
            assertThat(result.text()).isEqualTo("A: VALUE B: .....");
        }

        @Test
        void replacement_containing_special_regex_characters_is_safe() {
            // replacement strings with $, \, { etc. must not blow up
            SubstitutionResult result = PlaceholderProcessor.substituteEach("....", i -> "$100.00");
            assertThat(result.text()).isEqualTo("$100.00");
        }
    }

    // ================================================================== //
    //  substituteEachWithSpans                                             //
    // ================================================================== //

    @Nested
    class SubstituteEachWithSpans {

        @Test
        void no_placeholders_returns_empty_spans() {
            SubstitutionResultWithSpans result =
                    PlaceholderProcessor.substituteEachWithSpans("plain text", i -> "X");
            assertThat(result.text()).isEqualTo("plain text");
            assertThat(result.spans()).isEmpty();
            assertThat(result.anyFilled()).isFalse();
        }

        @Test
        void single_placeholder_span_has_correct_offsets() {
            // "Name: ...." → replace with "Alice"
            SubstitutionResultWithSpans result =
                    PlaceholderProcessor.substituteEachWithSpans("Name: ....", i -> "Alice");

            assertThat(result.text()).isEqualTo("Name: Alice");
            assertThat(result.filledCount()).isEqualTo(1);
            assertThat(result.anyFilled()).isTrue();

            SubstitutionSpan span = result.spans().getFirst();
            assertThat(span.originalStart()).isEqualTo(6);
            assertThat(span.originalEnd()).isEqualTo(10);
            assertThat(span.replacementLen()).isEqualTo(5); // "Alice".length()
            assertThat(span.replaced()).isTrue();
        }

        @Test
        void null_resolver_produces_span_with_replaced_false() {
            SubstitutionResultWithSpans result =
                    PlaceholderProcessor.substituteEachWithSpans("....", i -> null);

            assertThat(result.spans()).hasSize(1);
            assertThat(result.spans().getFirst().replaced()).isFalse();
            assertThat(result.anyFilled()).isFalse();
        }

        @Test
        void two_placeholders_produce_two_spans_in_order() {
            SubstitutionResultWithSpans result =
                    PlaceholderProcessor.substituteEachWithSpans(
                            "A: .... B: .....",
                            i -> i == 0 ? "X" : "YY");

            assertThat(result.spans()).hasSize(2);
            assertThat(result.spans().get(0).replaced()).isTrue();
            assertThat(result.spans().get(0).replacementLen()).isEqualTo(1);
            assertThat(result.spans().get(1).replaced()).isTrue();
            assertThat(result.spans().get(1).replacementLen()).isEqualTo(2);
        }
    }
}

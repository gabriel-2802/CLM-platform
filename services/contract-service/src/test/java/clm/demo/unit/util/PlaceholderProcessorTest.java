package clm.demo.unit.util;

import clm.demo.utils.file.PlaceholderProcessor;
import clm.demo.utils.file.PlaceholderProcessor.PlaceholderRecord;
import clm.demo.utils.file.PlaceholderProcessor.SubstitutionResult;
import clm.demo.utils.file.PlaceholderProcessor.SubstitutionResultWithSpans;
import clm.demo.utils.file.PlaceholderProcessor.SubstitutionSpan;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class PlaceholderProcessorTest {

    // ── normalize ─────────────────────────────────────────────────────────────

    @Test
    void normalize_returns_empty_string_for_null() {
        assertThat(PlaceholderProcessor.normalize(null)).isEmpty();
    }

    @Test
    void normalize_converts_crlf_to_lf() {
        assertThat(PlaceholderProcessor.normalize("a\r\nb")).isEqualTo("a\nb");
    }

    @Test
    void normalize_converts_cr_to_lf() {
        assertThat(PlaceholderProcessor.normalize("a\rb")).isEqualTo("a\nb");
    }

    @Test
    void normalize_replaces_horizontal_ellipsis_with_three_dots() {
        assertThat(PlaceholderProcessor.normalize("a…b")).isEqualTo("a...b");
    }

    @Test
    void normalize_replaces_two_dot_leader_with_two_dots() {
        assertThat(PlaceholderProcessor.normalize("a‥b")).isEqualTo("a..b");
    }

    @Test
    void normalize_replaces_fullwidth_full_stop_with_ascii_dot() {
        assertThat(PlaceholderProcessor.normalize("a．b")).isEqualTo("a.b");
    }

    @Test
    void normalize_replaces_midline_ellipsis_with_three_dots() {
        assertThat(PlaceholderProcessor.normalize("a⋯b")).isEqualTo("a...b");
    }

    @Test
    void normalize_leaves_plain_text_unchanged() {
        assertThat(PlaceholderProcessor.normalize("Hello World")).isEqualTo("Hello World");
    }

    // ── findPlaceholders ─────────────────────────────────────────────────────

    @Test
    void findPlaceholders_finds_single_4dot_sequence() {
        List<PlaceholderRecord> result = PlaceholderProcessor.findPlaceholders("Name: ....");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).occurrenceIndex()).isZero();
    }

    @Test
    void findPlaceholders_finds_multiple_placeholders() {
        List<PlaceholderRecord> result = PlaceholderProcessor.findPlaceholders("First: ...., Second: ....");
        assertThat(result).hasSize(2);
        assertThat(result.get(0).occurrenceIndex()).isZero();
        assertThat(result.get(1).occurrenceIndex()).isEqualTo(1);
    }

    @Test
    void findPlaceholders_returns_empty_when_no_placeholders() {
        assertThat(PlaceholderProcessor.findPlaceholders("No placeholders here")).isEmpty();
    }

    @Test
    void findPlaceholders_ignores_sequences_shorter_than_4_dots() {
        assertThat(PlaceholderProcessor.findPlaceholders("a...b")).isEmpty();
    }

    @Test
    void findPlaceholders_matches_5_dot_sequence() {
        List<PlaceholderRecord> result = PlaceholderProcessor.findPlaceholders(".....");
        assertThat(result).hasSize(1);
    }

    @Test
    void findPlaceholders_records_correct_offsets() {
        String text = "AB....CD";
        List<PlaceholderRecord> records = PlaceholderProcessor.findPlaceholders(text);
        assertThat(records).hasSize(1);
        assertThat(records.get(0).startOffset()).isEqualTo(2);
        assertThat(records.get(0).endOffset()).isEqualTo(6);
    }

    // ── substituteEach ───────────────────────────────────────────────────────

    @Test
    void substituteEach_replaces_placeholder_with_value() {
        SubstitutionResult result = PlaceholderProcessor.substituteEach("Hello ....", i -> "World");
        assertThat(result.text()).isEqualTo("Hello World");
        assertThat(result.filledCount()).isEqualTo(1);
    }

    @Test
    void substituteEach_keeps_original_when_resolver_returns_null() {
        SubstitutionResult result = PlaceholderProcessor.substituteEach("Hello ....", i -> null);
        assertThat(result.text()).isEqualTo("Hello ....");
        assertThat(result.filledCount()).isZero();
    }

    @Test
    void substituteEach_replaces_multiple_placeholders_in_order() {
        SubstitutionResult result = PlaceholderProcessor.substituteEach(
                "...., ...., ....",
                i -> switch (i) { case 0 -> "A"; case 1 -> "B"; default -> "C"; }
        );
        assertThat(result.text()).isEqualTo("A, B, C");
        assertThat(result.filledCount()).isEqualTo(3);
    }

    @Test
    void substituteEach_handles_empty_string() {
        SubstitutionResult result = PlaceholderProcessor.substituteEach("", i -> "value");
        assertThat(result.text()).isEmpty();
        assertThat(result.filledCount()).isZero();
    }

    @Test
    void substituteEach_replaces_partial_when_some_resolvers_return_null() {
        SubstitutionResult result = PlaceholderProcessor.substituteEach(
                "...., ...., ....",
                i -> i == 1 ? "MIDDLE" : null
        );
        assertThat(result.text()).isEqualTo("...., MIDDLE, ....");
        assertThat(result.filledCount()).isEqualTo(1);
    }

    // ── substituteEachWithSpans ───────────────────────────────────────────────

    @Test
    void substituteEachWithSpans_returns_spans_for_each_placeholder() {
        SubstitutionResultWithSpans result = PlaceholderProcessor.substituteEachWithSpans(
                "Name: ....", i -> "Alice");
        assertThat(result.filledCount()).isEqualTo(1);
        assertThat(result.spans()).hasSize(1);
        SubstitutionSpan span = result.spans().get(0);
        assertThat(span.replaced()).isTrue();
        assertThat(span.replacementLen()).isEqualTo("Alice".length());
    }

    @Test
    void substituteEachWithSpans_marks_unreplaced_span_as_not_replaced() {
        SubstitutionResultWithSpans result = PlaceholderProcessor.substituteEachWithSpans(
                "Name: ....", i -> null);
        assertThat(result.filledCount()).isZero();
        assertThat(result.spans().get(0).replaced()).isFalse();
    }

    @Test
    void substituteEachWithSpans_anyFilled_returns_false_when_nothing_replaced() {
        SubstitutionResultWithSpans result = PlaceholderProcessor.substituteEachWithSpans(
                "....", i -> null);
        assertThat(result.anyFilled()).isFalse();
    }

    @Test
    void substituteEachWithSpans_anyFilled_returns_true_when_at_least_one_replaced() {
        SubstitutionResultWithSpans result = PlaceholderProcessor.substituteEachWithSpans(
                "....", i -> "value");
        assertThat(result.anyFilled()).isTrue();
    }

    // ── Edge cases ────────────────────────────────────────────────────────────

    @ParameterizedTest
    @MethodSource("unicodePlaceholderInputs")
    void findPlaceholders_handles_unicode_edge_cases_after_normalize(String raw, int expectedCount) {
        String normalized = PlaceholderProcessor.normalize(raw);
        List<PlaceholderRecord> records = PlaceholderProcessor.findPlaceholders(normalized);
        assertThat(records).hasSize(expectedCount);
    }

    static Stream<Object[]> unicodePlaceholderInputs() {
        return Stream.of(
                new Object[]{"Name: ……b", 1},     // ellipsis × 2 = 6 dots after normalize → 1 match
                new Object[]{"Name: …b", 0},           // 3 dots after normalize → below threshold
                new Object[]{"....a....", 2},               // two separate 4-dot sequences
                new Object[]{"", 0}
        );
    }

    @Test
    void substituteEach_handles_special_characters_in_replacement() {
        SubstitutionResult result = PlaceholderProcessor.substituteEach(
                "Note: ....", i -> "O'Reilly & Sons $100 (Regex: $1)");
        assertThat(result.text()).isEqualTo("Note: O'Reilly & Sons $100 (Regex: $1)");
    }

    @Test
    void substituteEach_handles_very_long_replacement_value() {
        String longValue = "A".repeat(10_000);
        SubstitutionResult result = PlaceholderProcessor.substituteEach("....", i -> longValue);
        assertThat(result.text()).isEqualTo(longValue);
        assertThat(result.filledCount()).isEqualTo(1);
    }
}

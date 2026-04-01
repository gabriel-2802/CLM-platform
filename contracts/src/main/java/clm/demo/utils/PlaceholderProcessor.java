package clm.demo.utils;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.regex.Matcher;

import static clm.demo.utils.Constants.PLACEHOLDER_PATTERN;

/**
 * Utility methods for detecting and replacing dot-sequence placeholders in contract text.
 *
 */
@UtilityClass
public class PlaceholderProcessor {

    /**
     * Internal immutable record for placeholder data.
     */
    public record PlaceholderRecord(int occurrenceIndex, String prevText, int startOffset, int endOffset) {}

    /** Result of {@link #substituteEach}: the rewritten text plus how many placeholders were filled. */
    public record SubstitutionResult(String text, int filledCount) {}

    /**
     * Records one substitution: where the placeholder sat in the <em>original</em> string
     * and how long the replacement string is in the <em>rewritten</em> string.
     *
     * @param originalStart  inclusive start offset in the original text
     * @param originalEnd    exclusive end offset in the original text
     * @param replacementLen length of the string written at this position (value or original dots)
     * @param replaced       true if the placeholder was actually substituted with a value
     */
    public record SubstitutionSpan(int originalStart, int originalEnd, int replacementLen, boolean replaced) {}

    /** Extended result carrying per-span offset data for proportional run writeback. */
    public record SubstitutionResultWithSpans(String text, int filledCount, List<SubstitutionSpan> spans) {
        public boolean anyFilled() { return filledCount > 0; }
    }

    /**
     * Normalizes raw text so that placeholder matching is consistent:
     * <ol>
     *   <li>Line endings → {@code \n}</li>
     *   <li>Unicode dot-like glyphs → ASCII {@code .}:
     *     <ul>
     *       <li>U+2026 HORIZONTAL ELLIPSIS → {@code ...} (3 dots — its visual meaning)</li>
     *       <li>U+2025 TWO DOT LEADER      → {@code ..}  (2 dots)</li>
     *       <li>U+FF0E FULLWIDTH FULL STOP → {@code .}   (1 dot)</li>
     *       <li>U+22EF MIDLINE ELLIPSIS    → {@code ...} (3 dots)</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     *
     */
    public static String normalize(String raw) {
        if (raw == null) return "";
        return raw
                .replace("\r\n", "\n")
                .replace("\r",   "\n")
                // unicode dot-like glyphs → ASCII dots
                .replace("\u2026", "...") // HORIZONTAL ELLIPSIS  → 3 dots
                .replace("\u22EF", "...") // MIDLINE ELLIPSIS      → 3 dots
                .replace("\u2025", "..")  // TWO DOT LEADER        → 2 dots
                .replace("\uFF0E", ".");  // FULLWIDTH FULL STOP   → 1 dot
    }

    /**
     * Finds all placeholder occurrences in normalized text.
     *
     * @return ordered list of {@link PlaceholderRecord}, one per match
     */
    public static List<PlaceholderProcessor.PlaceholderRecord> findPlaceholders(String normalizedContent) {
        List<PlaceholderRecord> results = new ArrayList<>();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(normalizedContent);
        int position = 0;

        while (matcher.find()) {
            results.add(new PlaceholderRecord(
                    position++,
                    matcher.group(),
                    matcher.start(),
                    matcher.end()
            ));
        }

        return results;
    }

    /**
     * Replaces each placeholder with a value produced by {@code resolver},
     * which receives the zero-based occurrence index.
     * If {@code resolver} returns {@code null}, the original dot-sequence is kept.
     *
     * @param resolver function: occurrence index → replacement string (null = keep original)
     * @return (rewritten text, number of placeholders actually replaced)
     */
    public static SubstitutionResult substituteEach(String normalizedContent, IntFunction<String> resolver) {
        StringBuilder sb = new StringBuilder();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(normalizedContent);
        int index = 0;
        int replaced = 0;

        while (matcher.find()) {
            String value = resolver.apply(index++);
            if (value != null) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
                replaced++;
            } else {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group()));
            }
        }

        matcher.appendTail(sb);
        return new SubstitutionResult(sb.toString(), replaced);
    }

    /**
     * Same as {@link #substituteEach} but also returns per-substitution span data so
     * callers can map character positions in the original text to positions in the
     * rewritten text — enabling proportional writeback into individual Word runs
     * without corrupting per-run formatting.
     *
     * @param normalizedContent the merged paragraph text (must already be normalized)
     * @param resolver          occurrence-index → replacement string (null = keep original dots)
     * @return rewritten text, fill count, and one {@link SubstitutionSpan} per placeholder found
     */
    public static SubstitutionResultWithSpans substituteEachWithSpans(
            String normalizedContent, IntFunction<String> resolver) {

        StringBuilder sb      = new StringBuilder();
        Matcher matcher       = PLACEHOLDER_PATTERN.matcher(normalizedContent);
        List<SubstitutionSpan> spans = new ArrayList<>();
        int index    = 0;
        int replaced = 0;

        while (matcher.find()) {
            // capture original-string offsets BEFORE appendReplacement moves the matcher.
            int origStart = matcher.start();
            int origEnd   = matcher.end();

            String value      = resolver.apply(index++);
            boolean didReplace = value != null;
            String  actual    = didReplace ? value : matcher.group();

            matcher.appendReplacement(sb, Matcher.quoteReplacement(actual));

            // origStart / origEnd are fixed; replacementLen is the length written.
            spans.add(new SubstitutionSpan(origStart, origEnd, actual.length(), didReplace));

            if (didReplace) replaced++;
        }

        matcher.appendTail(sb);
        return new SubstitutionResultWithSpans(sb.toString(), replaced, spans);
    }
}
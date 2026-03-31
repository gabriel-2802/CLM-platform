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
 * <p>All methods expect <em>normalized</em> text — call {@link #normalize(String)} first
 * so that {@code startOffset}/{@code endOffset} offsets are consistent across platforms.</p>
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
     * Normalizes line endings to {@code \n}.
     * Must be called on raw extracted text before any other method here.
     */
    public static String normalize(String raw) {
        if (raw == null) return "";
        return raw.replace("\r\n", "\n").replace("\r", "\n");
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
     * Replaces every placeholder with a fixed literal string.
     */
    public static String substituteAll(String normalizedContent, String replacement) {
        if (normalizedContent == null) return "";
        return PLACEHOLDER_PATTERN.matcher(normalizedContent)
                .replaceAll(Matcher.quoteReplacement(replacement));
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
}
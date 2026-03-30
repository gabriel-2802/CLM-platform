package clm.demo.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;

import static clm.demo.utils.Constants.PLACEHOLDER_PATTERN;

/**
 * Utility methods for detecting and replacing dot-sequence placeholders in contract text.
 *
 * <p>All methods expect <em>normalized</em> text — call {@link #normalize(String)} first
 * so that {@code startIndex}/{@code endIndex} offsets are consistent across platforms.</p>
 */
@Slf4j
@UtilityClass
public class PlaceHolderUtils {

    /**
     * Normalizes line endings to {@code \n}.
     * Must be called on raw extracted text before any other method here,
     * and the same normalized string must be stored in the response so that
     * frontend highlight offsets align.
     */
    public static String normalize(String raw) {
        if (raw == null) return "";
        return raw.replace("\r\n", "\n").replace("\r", "\n");
    }

    /**
     * Finds all placeholder occurrences in normalized text.
     *
     * @param normalizedContent text returned by {@link #normalize(String)}
     * @return ordered list of {@link PlaceholderInfo}, one per match
     */
    public static List<PlaceholderInfo> findPlaceholders(String normalizedContent) {
        List<PlaceholderInfo> results = new ArrayList<>();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(normalizedContent);
        int position = 0;

        while (matcher.find()) {
            results.add(new PlaceholderInfo(
                    position++,
                    matcher.group(),
                    matcher.start(),
                    matcher.end(),
                    -1L
            ));
        }

        log.info("Found {} placeholder(s)", results.size());
        return results;
    }

    /**
     * Replaces every placeholder with a fixed literal string.
     *
     * @param normalizedContent text returned by {@link #normalize(String)}
     * @param replacement       literal replacement ({@code $} and {@code \} are safe)
     */
    public static String replaceAll(String normalizedContent, String replacement) {
        return PLACEHOLDER_PATTERN.matcher(normalizedContent)
                .replaceAll(Matcher.quoteReplacement(replacement));
    }

    /**
     * Replaces each placeholder with a value produced by {@code resolver},
     * which receives the zero-based occurrence index.
     *
     * <pre>{@code
     *   List<String> values = List.of("Alice", "2024-01-01");
     *   String filled = PlaceholderUtils.replaceEach(text, i -> values.get(i));
     * }</pre>
     *
     * <p>If {@code resolver} returns {@code null} for an index the original
     * dot sequence is kept unchanged.</p>
     *
     * @param normalizedContent text returned by {@link #normalize(String)}
     * @param resolver          function: occurrence index → replacement string
     */
    public static String replaceEach(String normalizedContent, Function<Integer, String> resolver) {
        StringBuffer sb = new StringBuffer();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(normalizedContent);
        int index = 0;

        while (matcher.find()) {
            String value = resolver.apply(index++);
            matcher.appendReplacement(sb,
                    Matcher.quoteReplacement(value != null ? value : matcher.group()));
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Replaces a single placeholder at zero-based {@code position}, leaving all others unchanged.
     *
     * @param normalizedContent text returned by {@link #normalize(String)}
     * @param position          zero-based index of the occurrence to replace
     * @param value             replacement string
     */
    public static String replaceAtPosition(String normalizedContent, int position, String value) {
        return replaceEach(normalizedContent, i -> i == position ? value : null);
    }


    @Data
    @AllArgsConstructor
    @Builder
    public static class PlaceholderInfo{
            int position;
            String placeholderText;
            int startIndex;
            int endIndex;
            Long fieldId;
    }
}
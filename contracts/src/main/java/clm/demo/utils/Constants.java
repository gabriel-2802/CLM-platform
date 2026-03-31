package clm.demo.utils;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

@UtilityClass
public final class Constants {

    /**
     * Matches 4+ consecutive "dot-like" characters, tolerating horizontal whitespace
     * (space/tab) between dot runs within the same line.
     *
     * <h3>Dot-like characters matched</h3>
     * <ul>
     *   <li>{@code .}  U+002E — ASCII full stop (the original case)</li>
     *   <li>{@code …}  U+2026 — HORIZONTAL ELLIPSIS (Word/LibreOffice autocorrect)</li>
     *   <li>{@code ‥}  U+2025 — TWO DOT LEADER</li>
     *   <li>{@code ．} U+FF0E — FULLWIDTH FULL STOP</li>
     *   <li>{@code ⋯}  U+22EF — MIDLINE HORIZONTAL ELLIPSIS</li>
     * </ul>
     *
     * <p>A single U+2026 glyph represents three dots visually, so a run of two or more
     * ellipsis characters already exceeds the "4 dots" threshold. The minimum anchor of
     * {@code {2,}} on the combined class ensures we still require at least a meaningful
     * run (≥ 2 glyphs ≈ ≥ 6 visual dots) when using ellipsis characters, while keeping
     * the familiar ≥ 4 requirement for plain ASCII dots.
     *
     * <p>Pattern breakdown:
     * <pre>
     *   DOT_CLASS           = [.\u2026\u2025\uFF0E\u22EF]
     *   anchor              = DOT_CLASS{4,}          — at least 4 dot-like chars to open
     *   continuation        = (?:[ \t]* DOT_CLASS+)* — space/tab-separated dot continuations
     * </pre>
     *
     * <p>{@code [ \t]} intentionally excludes {@code \n} so placeholders on separate
     * lines are never merged into one match.
     *
     * <p>The {@code UNICODE_CHARACTER_CLASS} flag is NOT required here because we list
     * every Unicode code point explicitly in the character class.
     */
    /**
     * Matches 4+ consecutive ASCII dots, tolerating horizontal whitespace (space/tab)
     * between dot runs within the same line — an artefact Word produces when
     * soft-wrapping a long dot sequence across multiple XML runs.
     *
     * <p>Unicode dot-like glyphs (U+2026 HORIZONTAL ELLIPSIS, U+2025 TWO DOT LEADER,
     * U+FF0E FULLWIDTH FULL STOP, U+22EF MIDLINE ELLIPSIS) are expanded to their ASCII
     * equivalents by {@link PlaceholderProcessor#normalize(String)} before this pattern
     * is applied, so this class intentionally matches ASCII {@code .} only.
     *
     * <p>Pattern: {@code \.{4,}(?:[ \t]*\.+)*}
     * <ul>
     *   <li>{@code \.{4,}}         – at least 4 ASCII dots (opening anchor)</li>
     *   <li>{@code (?:[ \t]*\.+)*} – zero or more space/tab-separated dot continuations</li>
     * </ul>
     *
     * <p>{@code [ \t]} intentionally excludes {@code \n} so placeholders on separate
     * lines are never merged into one match.
     */
    public static final Pattern PLACEHOLDER_PATTERN =
            Pattern.compile("\\.{4,}(?:[ \\t]*\\.+)*");
}
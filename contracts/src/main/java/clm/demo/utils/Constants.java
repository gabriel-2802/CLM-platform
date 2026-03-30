package clm.demo.utils;

import java.util.regex.Pattern;

public final class Constants {

    private Constants() {}

    /**
     * Matches 4+ consecutive dots, tolerating horizontal whitespace (space/tab)
     * between dot runs within the same paragraph — an artefact Word produces when
     * soft-wrapping a long dot sequence across multiple XML runs.
     *
     * <p>Pattern: {@code \.{4,}(?:[ \t]*\.+)*}
     * <ul>
     *   <li>{@code \.{4,}}       – at least 4 dots (opening anchor)</li>
     *   <li>{@code (?:[ \t]*\.+)*} – zero or more space/tab-separated dot continuations</li>
     * </ul>
     *
     * <p>{@code [ \t]} intentionally excludes {@code \n} so placeholders on separate
     * lines are never merged into one match.
     */
    public static final Pattern PLACEHOLDER_PATTERN =
            Pattern.compile("\\.{4,}(?:[ \\t]*\\.+)*");
}
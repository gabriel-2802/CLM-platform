package clm.demo.utils;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

/**
 * Application-wide constants.
 *
 * <p>This is a pure static utility class — no Spring context required. Keeping constants
 * here (rather than in a {@code @Component} with {@code @PostConstruct}-populated static
 * fields) means they are safely accessible from static utility methods, unit tests, and
 * any context where the Spring container is not running.</p>
 */
@UtilityClass
public class Constants {

    /** Zero-based default page index for paginated queries. */
    public static final int DEFAULT_PAGE      = 0;

    /** Default number of records returned per page. */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * Pattern that matches a placeholder: 4+ consecutive ASCII dots, with optional
     * horizontal whitespace between continuation dot-groups (e.g. {@code ".... ..."}).
     *
     * <p>Compiled once at class-load time; reused by all callers.</p>
     */
    public static final Pattern PLACEHOLDER_PATTERN =
            Pattern.compile("\\.{4,}(?:[ \\t]*\\.+)*+");
    /**
     * Canonical form that every placeholder is normalised to at upload time.
     * Exactly four ASCII dots.
     */
    public static final String PLACEHOLDER = "....";
}

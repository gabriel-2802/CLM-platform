package clm.demo.utils;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Utility class for constants
 */
@Component
public class Constants {

    public static final int DEFAULT_PAGE      = 0;
    public static final int DEFAULT_PAGE_SIZE = 20;

    @Value("${placeholder.pattern:\\.{4,}(?:[ \\t]*\\.+)*}")
    private String placeholderPatternString;

    @Value("${placeholder.default:....}")
    private String placeholderDefaultString;

    // static fields to hold the values after injection
    private static String PLACEHOLDER;
    private static Pattern PLACEHOLDER_PATTERN;

    @PostConstruct
    public void init() {
        PLACEHOLDER = placeholderDefaultString;
        PLACEHOLDER_PATTERN = Pattern.compile(placeholderPatternString);
    }

    public static Pattern getPlaceholderPattern() {
        return PLACEHOLDER_PATTERN;
    }

    public static String getPlaceholder() {
        return PLACEHOLDER;
    }

}

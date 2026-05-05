package clm.client.demo.exceptions;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ValidationErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<Map<String, String>> errors
) {
}

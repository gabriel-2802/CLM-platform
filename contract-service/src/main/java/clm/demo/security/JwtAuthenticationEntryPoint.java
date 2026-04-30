package clm.demo.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

/**
 * Returns a structured JSON 401 response when a request reaches a secured
 * endpoint without a valid Bearer token.
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final ObjectMapper MAPPER =
            new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_STATUS    = "status";
    private static final String KEY_ERROR     = "error";
    private static final String KEY_MESSAGE   = "message";
    private static final String KEY_PATH      = "path";
    private static final String ERROR_LABEL   = "Unauthorized";

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        log.warn("Unauthorized access to '{}': {}", request.getRequestURI(), authException.getMessage());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        var body = Map.of(
                KEY_TIMESTAMP, Instant.now().toString(),
                KEY_STATUS,    HttpServletResponse.SC_UNAUTHORIZED,
                KEY_ERROR,     ERROR_LABEL,
                KEY_MESSAGE,   authException.getMessage(),
                KEY_PATH,      request.getRequestURI()
        );

        MAPPER.writeValue(response.getOutputStream(), body);
    }
}
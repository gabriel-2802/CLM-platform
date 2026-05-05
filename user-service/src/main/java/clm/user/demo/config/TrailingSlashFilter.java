package clm.user.demo.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TrailingSlashFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path.length() > 1 && path.endsWith("/")) {
            String query = request.getQueryString();
            String stripped = path.substring(0, path.length() - 1);
            String redirectUrl = query != null ? stripped + "?" + query : stripped;
            response.sendRedirect(redirectUrl);
            return;
        }
        chain.doFilter(request, response);
    }
}

package com.PracticalAssignment.assignP.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final long WINDOW_MS = 60_000L; // 1 minute
    private static final int MAX_REQUESTS = 10; // per window per IP for auth endpoints

    private record Window(long windowStart, int count) {}

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // only apply rate limiting to sensitive auth endpoints
        return !(path.startsWith("/api/auth") || path.startsWith("/auth"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String ip = extractClientIp(request);
        long now = Instant.now().toEpochMilli();

        windows.compute(ip, (k, w) -> {
            if (w == null || now - w.windowStart() > WINDOW_MS) {
                return new Window(now, 1);
            }
            return new Window(w.windowStart(), w.count() + 1);
        });

        Window current = windows.get(ip);
        if (current.count() > MAX_REQUESTS) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write('{'+"\"error\":\"Too many requests\""+'}');
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String extractClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}

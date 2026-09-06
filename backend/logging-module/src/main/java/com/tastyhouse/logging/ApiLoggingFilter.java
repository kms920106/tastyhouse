package com.tastyhouse.logging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiLoggingFilter.class);
    private static final String MDC_REQUEST_ID = "requestId";
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String REQUEST_START_TIME_ATTR = "requestStartTime";

    private static final int MAX_BODY_LOG_SIZE = 2048;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(MDC_REQUEST_ID, requestId);
        request.setAttribute(REQUEST_START_TIME_ATTR, System.currentTimeMillis());

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            log.info("[REQUEST] {} {} | IP: {}",
                    wrappedRequest.getMethod(),
                    wrappedRequest.getRequestURI(),
                    resolveClientIp(wrappedRequest));

            filterChain.doFilter(wrappedRequest, wrappedResponse);

        } finally {
            long elapsed = System.currentTimeMillis() - (long) request.getAttribute(REQUEST_START_TIME_ATTR);
            int status = wrappedResponse.getStatus();

            if (log.isDebugEnabled()) {
                logRequestBody(wrappedRequest);
                logResponseBody(wrappedResponse);
            }

            logResponse(wrappedRequest.getMethod(), wrappedRequest.getRequestURI(), status, elapsed);

            wrappedResponse.copyBodyToResponse();

            MDC.remove(MDC_REQUEST_ID);
        }
    }

    private void logRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length == 0) {
            return;
        }
        String body = new String(content, StandardCharsets.UTF_8);
        if (body.length() > MAX_BODY_LOG_SIZE) {
            body = body.substring(0, MAX_BODY_LOG_SIZE) + "... [truncated]";
        }
        log.debug("[REQUEST BODY] {}", body);
    }

    private void logResponseBody(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        if (content.length == 0) {
            return;
        }
        String body = new String(content, StandardCharsets.UTF_8);
        if (body.length() > MAX_BODY_LOG_SIZE) {
            body = body.substring(0, MAX_BODY_LOG_SIZE) + "... [truncated]";
        }
        log.debug("[RESPONSE BODY] {}", body);
    }

    private void logResponse(String method, String uri, int status, long elapsed) {
        String message = "[RESPONSE] {} {} | {} | {}ms";
        if (status >= 500) {
            log.error(message, method, uri, status, elapsed);
        } else if (status >= 400) {
            log.warn(message, method, uri, status, elapsed);
        } else {
            log.info(message, method, uri, status, elapsed);
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader(X_FORWARDED_FOR);
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

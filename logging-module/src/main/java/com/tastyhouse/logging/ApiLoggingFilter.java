package com.tastyhouse.logging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * API 요청/응답 메타 정보 로깅 필터
 * 로깅 항목: requestId, Method, Path, Client IP, Status Code, 처리 시간(ms)
 * requestId는 MDC에 등록되어 같은 요청에서 발생하는 모든 로그(p6spy 포함)에 자동 첨부됨
 * Body 로깅: DEBUG 레벨 (개발 환경에서만 활성화)
 * - application-dev.yml: com.tastyhouse.logging: DEBUG
 * - application-prod.yml: com.tastyhouse.logging: INFO (기본값 유지)
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiLoggingFilter extends OncePerRequestFilter {

    private static final String MDC_REQUEST_ID = "requestId";
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String REQUEST_START_TIME_ATTR = "requestStartTime";

    // Body 로그 최대 출력 크기 (초과 시 truncate)
    private static final int MAX_BODY_LOG_SIZE = 2048;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(MDC_REQUEST_ID, requestId);
        request.setAttribute(REQUEST_START_TIME_ATTR, System.currentTimeMillis());

        // Body 로깅이 필요한 경우에만 Wrapper로 감싸서 스트림 재사용 가능하게 함
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

            // DEBUG 레벨일 때만 Body 로깅 (개발 환경)
            if (log.isDebugEnabled()) {
                logRequestBody(wrappedRequest);
                logResponseBody(wrappedResponse);
            }

            logResponse(wrappedRequest.getMethod(), wrappedRequest.getRequestURI(), status, elapsed);

            // ContentCachingResponseWrapper는 응답 본문을 내부에 캐싱하므로
            // 실제 클라이언트에게 응답을 전달하려면 반드시 copyBodyToResponse() 호출 필요
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

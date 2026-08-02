package com.tastyhouse.security.jwt;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 권한이 부족한 요청(필터 단계)을 403 {@link ProblemDetail}로 응답한다.
 *
 * <p>{@code errorCode} property를 함께 담아 전역 예외 핸들러(advice 단계)의 403 응답과 스키마를 일치시킨다.
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public JwtAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.FORBIDDEN,
            ErrorCode.ACCESS_DENIED.getDefaultMessage()
        );
        problemDetail.setProperty("errorCode", ErrorCode.ACCESS_DENIED.getCode());
        response.getWriter().write(objectMapper.writeValueAsString(problemDetail));
    }
}

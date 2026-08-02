package com.tastyhouse.security.jwt;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 인증되지 않은 요청(필터 단계)을 401 {@link ProblemDetail}로 응답한다.
 *
 * <p>{@code errorCode} property를 함께 담아 전역 예외 핸들러(advice 단계)의 401 응답과 스키마를 일치시킨다.
 * 필터 단계는 advice를 타지 않아 응답을 직접 직렬화하지만, 클라이언트가 보는 계약은 같아야 한다.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED,
            ErrorCode.AUTH_REQUIRED.getDefaultMessage()
        );
        problemDetail.setProperty("errorCode", ErrorCode.AUTH_REQUIRED.getCode());
        response.getWriter().write(objectMapper.writeValueAsString(problemDetail));
    }
}

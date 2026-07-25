package com.tastyhouse.ceoapi.exception;

import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.security.ratelimit.RateLimitException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusinessException(BusinessException e) {
        log.warn("BusinessException [{}]: {}", e.getErrorCode().getCode(), e.getMessage());
        return problemDetail(e.getErrorCode().getHttpStatusCode(), e.getErrorCode().getCode(), e.getMessage());
    }

    @ExceptionHandler(RateLimitException.class)
    public ProblemDetail handleRateLimitException(RateLimitException e) {
        log.warn("RateLimitException: {}", e.getMessage());
        return problemDetail(
            HttpStatus.TOO_MANY_REQUESTS.value(),
            ErrorCode.RATE_LIMIT_EXCEEDED.getCode(),
            ErrorCode.RATE_LIMIT_EXCEEDED.getDefaultMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(" "));
        log.warn("Validation failed: {}", message);
        return problemDetail(HttpStatus.BAD_REQUEST.value(), null, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("Request body not readable: {}", e.getMessage());
        return problemDetail(HttpStatus.BAD_REQUEST.value(), null, "요청 본문을 읽을 수 없습니다.");
    }

    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ProblemDetail handleBadCredentials(Exception e) {
        log.warn("Authentication failed: {}", e.getMessage());
        return problemDetail(HttpStatus.UNAUTHORIZED.value(), null, "아이디 또는 비밀번호가 올바르지 않습니다.");
    }

    @ExceptionHandler({DisabledException.class, LockedException.class})
    public ProblemDetail handleDisabled(Exception e) {
        log.warn("Account unavailable: {}", e.getMessage());
        return problemDetail(HttpStatus.UNAUTHORIZED.value(), null, "비활성화되었거나 잠긴 계정입니다.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());
        return problemDetail(HttpStatus.FORBIDDEN.value(), null, "접근 권한이 없습니다.");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception e) {
        log.error("Unexpected error", e);
        return problemDetail(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, "서버 오류가 발생했습니다.");
    }

    private ProblemDetail problemDetail(int statusCode, String errorCode, String message) {
        HttpStatus status = HttpStatus.resolve(statusCode);
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, message);
        if (errorCode != null) {
            problemDetail.setProperty("errorCode", errorCode);
        }
        return problemDetail;
    }
}

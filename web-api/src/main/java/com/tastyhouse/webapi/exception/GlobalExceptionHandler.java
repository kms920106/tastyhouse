package com.tastyhouse.webapi.exception;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.external.exception.ExternalApiException;
import com.tastyhouse.security.ratelimit.RateLimitException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusinessException(BusinessException e) {
        log.warn("BusinessException [{}]: {}", e.getErrorCode().getCode(), e.getMessage());
        return problemDetail(e.getErrorCode().getHttpStatusCode(), e.getErrorCode().getCode(), e.getMessage());
    }

    @ExceptionHandler(ExternalApiException.class)
    public ProblemDetail handleExternalApiException(ExternalApiException e) {
        log.warn("ExternalApiException [{}]: {}", e.getErrorCode().getCode(), e.getMessage());
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

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentialsException(BadCredentialsException e) {
        log.warn("BadCredentialsException: {}", e.getMessage());
        return problemDetail(HttpStatus.UNAUTHORIZED.value(), null, "아이디 또는 비밀번호가 올바르지 않습니다.");
    }

    @ExceptionHandler(DisabledException.class)
    public ProblemDetail handleDisabledException(DisabledException e) {
        log.warn("DisabledException: {}", e.getMessage());
        return problemDetail(HttpStatus.UNAUTHORIZED.value(), null, "비활성화된 계정입니다.");
    }

    @ExceptionHandler(LockedException.class)
    public ProblemDetail handleLockedException(LockedException e) {
        log.warn("LockedException: {}", e.getMessage());
        return problemDetail(HttpStatus.UNAUTHORIZED.value(), null, "잠긴 계정입니다.");
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ProblemDetail handleUnauthorizedException(UnauthorizedException e) {
        log.warn("UnauthorizedException: {}", e.getMessage());
        return problemDetail(HttpStatus.UNAUTHORIZED.value(), null, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
            .collect(Collectors.joining(", "));
        log.warn("MethodArgumentNotValidException: {}", message);
        return problemDetail(HttpStatus.BAD_REQUEST.value(), null, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("HttpMessageNotReadableException: {}", e.getMessage());
        return problemDetail(HttpStatus.BAD_REQUEST.value(), null, "요청 본문을 읽을 수 없습니다. JSON 형식을 확인해주세요.");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        String message = String.format("필수 파라미터 '%s'가 누락되었습니다.", e.getParameterName());
        log.warn("MissingServletRequestParameterException: {}", message);
        return problemDetail(HttpStatus.BAD_REQUEST.value(), null, message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String message = String.format("파라미터 '%s'의 값 '%s'이(가) 올바르지 않습니다.", e.getName(), e.getValue());
        log.warn("MethodArgumentTypeMismatchException: {}", message);
        return problemDetail(HttpStatus.BAD_REQUEST.value(), null, message);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ProblemDetail handleNoHandlerFoundException(NoHandlerFoundException e) {
        String message = String.format("요청하신 API를 찾을 수 없습니다: %s %s", e.getHttpMethod(), e.getRequestURL());
        log.warn("NoHandlerFoundException: {}", message);
        return problemDetail(HttpStatus.NOT_FOUND.value(), null, message);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception e) {
        log.error("Unexpected error occurred", e);
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

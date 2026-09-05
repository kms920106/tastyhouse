package com.tastyhouse.apicommon.exception;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.apicommon.ratelimit.RateLimitException;

/**
 * admin-api·ceo-api 공용 전역 예외 핸들러.
 *
 * <p><b>web-api는 이 핸들러를 쓰지 않는다.</b> 검증 실패 메시지 형식이 web-api는 {@code "필드명: 메시지"}를
 * {@code ", "}로 join하는 반면 여기서는 메시지만 공백 join하는 등 <b>응답 계약이 다르다.</b> 이는 우연한 차이가
 * 아니라 소비자별 계약 차이이므로 통합하지 않고 web-api가 자체 핸들러를 유지한다
 * ({@code com.tastyhouse.webapi.exception.GlobalExceptionHandler}). 그래서 {@code WebApiApplication}은
 * 계약이 아니라 조립 로직인 {@link ProblemDetails}는 두 핸들러가 공유한다.
 *
 * <p><b>이 핸들러의 등록 주체는 {@link com.tastyhouse.apicommon.ApiCommonModuleAutoConfiguration}이다.</b>
 * 컴포넌트 스캔이 아니라 {@code @Bean("sharedGlobalExceptionHandler")} + {@code @ConditionalOnMissingBean
 * (annotation = RestControllerAdvice.class)}로 등록되므로, 자체 advice를 가진 web-api에서는 자동으로
 * 물러난다. 앱이 {@code @Import}로 조립하던 시절과 달리 앱 쪽에 배선 코드가 없다.
 *
 * <p>{@code ExternalApiException}은 {@code BusinessException}을 상속하므로 아래
 * {@link #handleBusinessException} 하나로 처리된다(전용 핸들러 불필요).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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
        return problemDetail(
            ErrorCode.ACCESS_DENIED.getHttpStatusCode(),
            ErrorCode.ACCESS_DENIED.getCode(),
            ErrorCode.ACCESS_DENIED.getDefaultMessage()
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParameter(MissingServletRequestParameterException e) {
        String message = String.format("필수 파라미터 '%s'가 누락되었습니다.", e.getParameterName());
        log.warn("Missing request parameter: {}", message);
        return problemDetail(HttpStatus.BAD_REQUEST.value(), null, message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String message = String.format("파라미터 '%s'의 값 '%s'이(가) 올바르지 않습니다.", e.getName(), e.getValue());
        log.warn("Parameter type mismatch: {}", message);
        return problemDetail(HttpStatus.BAD_REQUEST.value(), null, message);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ProblemDetail handleNoHandlerFound(NoHandlerFoundException e) {
        String message = String.format("요청하신 API를 찾을 수 없습니다: %s %s", e.getHttpMethod(), e.getRequestURL());
        log.warn("No handler found: {}", message);
        return problemDetail(HttpStatus.NOT_FOUND.value(), null, message);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception e) {
        log.error("Unexpected error", e);
        return problemDetail(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, "서버 오류가 발생했습니다.");
    }

    private ProblemDetail problemDetail(int statusCode, String errorCode, String message) {
        return ProblemDetails.of(statusCode, errorCode, message);
    }
}

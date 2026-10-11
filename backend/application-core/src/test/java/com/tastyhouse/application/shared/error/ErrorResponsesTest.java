package com.tastyhouse.application.shared.error;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shared.exception.WebErrorCode;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponsesTest {

    @Test
    @DisplayName("최상위 예외가 ApplicationException이면 에러코드와 예외 메시지로 판정한다")
    void resolvesTopLevelApplicationException() {
        Optional<ErrorDescriptor> resolved =
            ErrorResponses.resolve(new ApplicationException(ApplicationErrorCode.INVALID_INPUT, "사용자 지정 메시지"));

        assertThat(resolved).contains(new ErrorDescriptor(
            ApplicationErrorCode.INVALID_INPUT.getHttpStatusCode(), ApplicationErrorCode.INVALID_INPUT.getCode(), "사용자 지정 메시지"));
    }

    @Test
    @DisplayName("DomainException도 같은 규칙으로 판정한다")
    void resolvesDomainException() {
        Optional<ErrorDescriptor> resolved =
            ErrorResponses.resolve(new DomainException(DomainErrorCode.POINT_INSUFFICIENT));

        assertThat(resolved).contains(new ErrorDescriptor(
            DomainErrorCode.POINT_INSUFFICIENT.getHttpStatusCode(), DomainErrorCode.POINT_INSUFFICIENT.getCode(),
            DomainErrorCode.POINT_INSUFFICIENT.getDefaultMessage()));
    }

    @Test
    @DisplayName("ApplicationException 하위 타입과 앱 모듈 에러코드도 판정한다")
    void resolvesApplicationExceptionSubtype() {
        Optional<ErrorDescriptor> resolved =
            ErrorResponses.resolve(new ResourceNotFoundException(WebErrorCode.RESERVATION_NOT_FOUND));

        assertThat(resolved).contains(new ErrorDescriptor(
            WebErrorCode.RESERVATION_NOT_FOUND.getHttpStatusCode(), WebErrorCode.RESERVATION_NOT_FOUND.getCode(),
            WebErrorCode.RESERVATION_NOT_FOUND.getDefaultMessage()));
    }

    @Test
    @DisplayName("BusinessException을 원인으로 감싼 예외는 판정하지 않는다")
    void doesNotFollowCause() {
        RuntimeException wrapped = new IllegalStateException(new ApplicationException(ApplicationErrorCode.INVALID_INPUT));

        assertThat(ErrorResponses.resolve(wrapped)).isEmpty();
    }
}

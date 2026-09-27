package com.tastyhouse.application.shared.error;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorContractsConsistencyTest {

    @Test
    @DisplayName("ErrorContracts의 세 계약은 대응하는 ErrorCode의 status·code·defaultMessage와 같다")
    void contractsShouldMirrorErrorCodes() {
        assertMirrors(ErrorContracts.rateLimit(), ErrorCode.RATE_LIMIT_EXCEEDED);
        assertMirrors(ErrorContracts.accessDenied(), ErrorCode.ACCESS_DENIED);
        assertMirrors(ErrorContracts.authRequired(), ErrorCode.AUTH_REQUIRED);
    }

    @Test
    @DisplayName("최상위 예외가 BusinessException이면 에러코드와 예외 메시지로 판정한다")
    void resolvesTopLevelBusinessException() {
        Optional<ErrorDescriptor> resolved =
            ErrorResponses.resolve(new BusinessException(ErrorCode.INVALID_INPUT, "사용자 지정 메시지"));

        assertThat(resolved).contains(new ErrorDescriptor(
            ErrorCode.INVALID_INPUT.getHttpStatusCode(), ErrorCode.INVALID_INPUT.getCode(), "사용자 지정 메시지"));
    }

    @Test
    @DisplayName("BusinessException 하위 타입도 판정한다")
    void resolvesBusinessExceptionSubtype() {
        Optional<ErrorDescriptor> resolved =
            ErrorResponses.resolve(new ResourceNotFoundException(ErrorCode.FILE_NOT_FOUND));

        assertThat(resolved).contains(new ErrorDescriptor(
            ErrorCode.FILE_NOT_FOUND.getHttpStatusCode(), ErrorCode.FILE_NOT_FOUND.getCode(),
            ErrorCode.FILE_NOT_FOUND.getDefaultMessage()));
    }

    @Test
    @DisplayName("BusinessException을 원인으로 감싼 예외는 판정하지 않는다")
    void doesNotFollowCause() {
        RuntimeException wrapped = new IllegalStateException(new BusinessException(ErrorCode.INVALID_INPUT));

        assertThat(ErrorResponses.resolve(wrapped)).isEmpty();
    }

    private void assertMirrors(ErrorDescriptor contract, ErrorCode errorCode) {
        assertThat(contract).isEqualTo(new ErrorDescriptor(
            errorCode.getHttpStatusCode(), errorCode.getCode(), errorCode.getDefaultMessage()));
    }
}

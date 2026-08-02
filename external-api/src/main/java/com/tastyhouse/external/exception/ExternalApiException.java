package com.tastyhouse.external.exception;

import com.tastyhouse.domain.exception.BusinessException;

/**
 * 외부 연동(SMS·메일 발송 등) 실패 예외.
 *
 * <p>{@link BusinessException}을 상속하므로 각 api 모듈의 {@code BusinessException} 핸들러가 그대로 처리한다.
 * 과거에는 독립 예외라 모듈별 전용 핸들러가 필요했고, admin-api·ceo-api가 그 핸들러를 갖지 않아
 * 502로 의도된 발송 실패가 {@code Exception} 폴백을 타고 500으로 응답되는 결함이 있었다.
 */
public class ExternalApiException extends BusinessException {

    public ExternalApiException(ExternalApiErrorCode errorCode) {
        super(errorCode);
    }

    public ExternalApiException(ExternalApiErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ExternalApiException(ExternalApiErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}

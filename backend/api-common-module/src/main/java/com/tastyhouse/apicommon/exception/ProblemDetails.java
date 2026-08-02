package com.tastyhouse.apicommon.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/**
 * 전역 예외 핸들러가 공유하는 RFC7807 {@link ProblemDetail} 조립 유틸.
 *
 * <p>web-api와 admin-api·ceo-api는 응답 계약(검증 실패 메시지 형식 등)이 달라 전역 핸들러를 각자 유지하지만,
 * 이 조립 로직만은 두 핸들러에 바이트 단위로 동일하게 복제돼 있었다. 계약 차이는 메시지를 만드는 쪽에 있고
 * 조립 자체에는 없으므로 이 유틸 하나로 통합한다.
 *
 * <p>{@code @Component}가 아닌 static 유틸이므로 컴포넌트 스캔 범위와 무관하다
 * (web-api는 {@code com.tastyhouse.apicommon.file}만 스캔해 이 패키지의 핸들러 빈을 등록하지 않는다).
 */
public final class ProblemDetails {

    private ProblemDetails() {
    }

    public static ProblemDetail of(int statusCode, String errorCode, String message) {
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

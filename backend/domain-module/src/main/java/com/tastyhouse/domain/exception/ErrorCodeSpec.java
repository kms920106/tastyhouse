package com.tastyhouse.domain.exception;

/**
 * 에러코드가 응답 계약으로 노출해야 하는 최소 계약.
 *
 * <p>{@link ErrorCode}(비즈니스 에러 카탈로그)와 {@code ExternalApiErrorCode}(외부 연동 에러)가 각각 이 인터페이스를
 * 구현한다. 두 enum은 구조가 동일했음에도 공통 추상이 없어, 각 api 모듈의 전역 예외 핸들러가 예외 타입마다
 * 같은 변환 코드를 복제해야 했고 그 결과 admin-api·ceo-api에서 {@code ExternalApiException} 핸들러가 누락되어
 * 502로 의도된 외부 연동 실패가 500으로 응답되는 결함이 있었다. 이 인터페이스를 도입하고
 * {@link BusinessException}이 이를 보유하도록 하면 {@code BusinessException} 핸들러 하나가 모든 에러코드
 * 계열을 처리한다.
 *
 * <p>{@code httpStatusCode}가 {@code org.springframework.http.HttpStatus}가 아닌 {@code int}인 이유는
 * domain-module이 프레임워크-프리(production 의존 0)이기 때문이다. HTTP 상태 해석은 api 모듈의 핸들러가 담당한다.
 */
public interface ErrorCodeSpec {

    int getHttpStatusCode();

    String getCode();

    String getDefaultMessage();
}

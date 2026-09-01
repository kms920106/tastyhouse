package com.tastyhouse.application.shop.port.out;

/**
 * 가게 노출 상태 — 노출정지·폐업 여부.
 *
 * <p><b>챕터 09</b>에서 신설. 이 두 값은 소유권 검증이 반환한 {@code Shop} 애그리거트에서 읽으므로
 * 조회 포트의 Result가 아니라 <b>도메인 모델</b>에서 나온다. api 모듈은 도메인 모델을 알 수 없어
 * ({@code apiModuleShouldBeDomainModelFree}) 표현 계약이 {@code Shop}을 직접 받을 수 없으므로,
 * application이 애그리거트에서 꺼낸 값을 이 record에 담아 넘긴다.
 */
public record ShopStatusResult(
    boolean hidden,
    boolean permanentlyClosed
) {
}

package com.tastyhouse.application.shop.port.out;

import java.math.BigDecimal;

/**
 * 점주 내 가게 상세 — 애그리거트에서 읽은 가게 정보와 별도 조회한 이미지 URL.
 *
 * <p><b>챕터 09</b>에서 신설. 가게 정보는 소유권 검증이 반환한 {@code Shop} <b>애그리거트</b>에서
 * 나오고 이미지 URL은 {@code ShopBasicInfoQueryPort}에서 나온다. 표현 계약은 도메인 모델을 알 수
 * 없으므로({@code apiModuleShouldBeDomainModelFree}) application이 두 출처를 이 record로 합쳐 넘긴다.
 *
 * <p>컴포넌트 순서는 기존 {@code ShopDetailResponse}의 필드 순서를 그대로 승계한다 — 응답 JSON의
 * 필드 구성이 불변이어야 하고, 같은 타입이 연속하는 자리에서 순서가 어긋나면 컴파일은 통과한 채
 * 값만 조용히 뒤바뀐다.
 */
public record ShopDetailViewResult(
    Long id,
    Long stationId,
    String name,
    BigDecimal latitude,
    BigDecimal longitude,
    Double rating,
    String roadAddress,
    String lotAddress,
    String phoneNumber,
    String thumbnailImageUrl,
    String trademarkImageUrl,
    boolean permanentlyClosed,
    boolean hidden,
    boolean closedOnPublicHolidays,
    int minOrderAmount,
    boolean scheduledOrderEnabled,
    boolean cupDepositEnabled
) {
}

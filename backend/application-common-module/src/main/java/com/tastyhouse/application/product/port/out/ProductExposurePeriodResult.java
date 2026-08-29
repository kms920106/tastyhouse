package com.tastyhouse.application.product.port.out;

import java.time.LocalDate;

/**
 * 메뉴에 현재 설정된 노출기간(기간 축) 투영.
 *
 * <p>{@code startDate}·{@code endDate}가 모두 {@code null}이면 기간 제약이 없다. 요일·시간대 축은
 * 자식 테이블({@code PRODUCT_EXPOSURE_HOUR})에 있어 이 투영에 담지 않는다.
 *
 * <p>{@code shopId}를 함께 담는 이유는 소비 측(ceo-api)이 <b>이 메뉴가 정말 그 가게 것인지</b>
 * 재확인해야 하기 때문이다 — 경로의 메뉴 id와 query의 가게 id가 서로를 검증하지 않으면 IDOR이 된다.
 */
public record ProductExposurePeriodResult(
    Long productId,
    Long shopId,
    LocalDate startDate,
    LocalDate endDate
) {
}

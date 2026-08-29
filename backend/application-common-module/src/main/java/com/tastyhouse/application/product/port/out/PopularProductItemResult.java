package com.tastyhouse.application.product.port.out;

import java.math.BigDecimal;

/**
 * 인기 메뉴 그룹(가게 상세 상단 "가장 인기 있는 메뉴") 항목 read model.
 *
 * <p>{@code representative}는 이 항목이 <b>사장님 추천으로 채워진 자리인지</b>를 뜻한다. 화면이 추천
 * 뱃지를 붙일지 판단하는 데 쓰며, 조합 규칙(추천 우선 → 남는 자리를 판매량 순으로)의 결과를 그대로
 * 드러낸다.
 *
 * <p>{@code salesQuantity}는 집계 창(최근 30일, 완료 주문) 안의 판매 수량 합이다. 추천으로 채워진
 * 항목은 판매 이력이 없을 수 있어 {@code 0}이 될 수 있다 — 그때도 자리를 잃지 않는 것이 조합 규칙의
 * 요점이다.
 */
public record PopularProductItemResult(
    Long id,
    String name,
    String imageUrl,
    Integer originalPrice,
    Integer discountPrice,
    BigDecimal discountRate,
    Double rating,
    Integer reviewCount,
    boolean representative,
    Integer spiciness,
    long salesQuantity
) {
}

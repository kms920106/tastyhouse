package com.tastyhouse.webapplication.product.port.out;

import java.math.BigDecimal;
import java.util.List;

/**
 * 손님용 메뉴 상세 — 상세 투영·메뉴 평가 수·해석된 가격 행을 한 벌로 묶은 조회 결과.
 *
 * <p><b>챕터 10</b>에서 신설. 공용 읽기 계약 {@code ProductDetailResult} 하나로는 이 응답을 표현할 수
 * 없다 — {@code menuReviewCount}는 다른 포트({@code MenuReviewStatisticsQueryPort})의 조회에
 * null 기본값 0을 씌운 <b>파생값</b>이고, {@code prices}는 도메인 계산으로 해석된
 * {@link ProductPriceView} 목록이라 세 출처를 서비스가 합쳐야 한다.
 *
 * <p>{@code originalPrice}·{@code discountPrice}·{@code discountRate}를 {@code prices}와 함께 남기는
 * 이유는 {@code ProductDetailResponse}의 Javadoc에 있다 — 여러 화면이 읽는 기존 계약이고, 가격 행이
 * 없는 이관 이전 메뉴는 이 필드들로만 동작한다.
 */
public record ProductDetailView(
    Long id,
    String name,
    String description,
    Integer originalPrice,
    Integer discountPrice,
    BigDecimal discountRate,
    boolean soldOut,
    String weightText,
    long menuReviewCount,
    List<ProductPriceView> prices
) {
}

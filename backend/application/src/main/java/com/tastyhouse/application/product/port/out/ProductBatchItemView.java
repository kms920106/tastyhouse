package com.tastyhouse.application.product.port.out;

import java.util.List;

/**
 * 배치 조회 상품 한 건 — 상품 투영에 해석된 가격 행 목록을 붙인 조회 결과.
 *
 * <p><b>챕터 10</b>에서 신설. 공용 읽기 계약 {@code ProductBatchResult}는 가격 행을 품지 않는다 —
 * 가격 행은 N+1을 피하려고 별도 포트 호출로 한 번에 읽어 {@code productId}로 묶은 뒤 붙이고, 그
 * 해석은 도메인 계산이라 서비스에서 끝나야 한다({@link ProductPriceView}).
 *
 * <p>{@code available=false}(판매 종료·미존재)면 나머지 필드는 비어 있고 {@code options}·{@code prices}는
 * 빈 목록이다 — 요청 순서를 유지한 채 화면이 "판매 종료"를 안내하게 하려는 것이다.
 *
 * <p>{@code options}는 공용 계약 {@code BatchOptionResult}를 그대로 나른다 — 보증금·개인컵 할인 금액이
 * 이미 그 계약에 담겨 있어 다시 옮길 이유가 없다.
 */
public record ProductBatchItemView(
    Long id,
    boolean available,
    String name,
    String imageUrl,
    Integer originalPrice,
    Integer discountPrice,
    List<BatchOptionResult> options,
    List<ProductPriceView> prices
) {
}

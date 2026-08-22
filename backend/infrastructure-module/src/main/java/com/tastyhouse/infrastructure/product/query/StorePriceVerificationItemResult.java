package com.tastyhouse.infrastructure.product.query;

/**
 * 매장 가격 인증 요청의 대상 항목 투영 — 검수자가 <b>앱 가격과 신고된 매장가를 대조</b>하는 데 쓴다.
 *
 * <p>그래서 요청 행이 박제한 {@code storePrice}만이 아니라 현재 가격 행의 {@code deliveryPrice}도 함께
 * 담는다. 둘 중 하나만 보이면 "이 값이 타당한가"를 판정할 근거가 없다.
 *
 * <p>{@code deliveryPrice}는 <b>현재</b> 가격 행의 값이므로 요청 시점 값과 다를 수 있다. 그것이 의도다 —
 * 검수 시점의 앱 노출가와 대조해야 하기 때문이며, 반영 대상인 {@code storePrice}만 요청 시점에 박제된다.
 */
public record StorePriceVerificationItemResult(
    Long productId,
    String productName,
    Long priceId,
    String priceName,
    Integer storePrice,
    Integer deliveryPrice,
    boolean applyPickupSamePrice
) {

}

package com.tastyhouse.domain.order.service;

/**
 * 주문 라인 하나의 선택 옵션에서 나온 금액 세 갈래(수량 곱 전).
 *
 * <p><b>세 값을 함께 돌려주는 이유</b>는 각각이 서로 다른 금액 항목으로 흘러가기 때문이다.
 * <ul>
 *   <li>{@code totalOptionPrice} → 상품 금액({@code totalProductAmount})에 가산 — 과세 매출이다.</li>
 *   <li>{@code totalDepositAmount} → <b>상품 금액이 아니라</b> {@code cupDepositAmount}로 분리.
 *       비과세·정산 제외 항목이라 상품 금액에 섞으면 최소주문금액·쿠폰·포인트 기준액이 오염된다.</li>
 *   <li>{@code totalPersonalCupDiscount} → 상품 할인({@code productDiscountAmount})에 가산.
 *       개인컵 할인은 보증금 축이 아니라 <b>할인 축</b>이므로, 여기에 넣어야 총할인 불변식과 매출 차감이
 *       정상적으로 성립한다.</li>
 * </ul>
 *
 * <p>하나로 합쳐 돌려주면 호출부가 다시 나눌 근거를 잃고, 그 순간 이 분리 설계 전체가 무너진다.
 */
public record OrderLineOptionAmounts(
    int totalOptionPrice,
    int totalDepositAmount,
    int totalPersonalCupDiscount
) {
}

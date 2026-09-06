package com.tastyhouse.domain.order.service;

import java.util.List;

/**
 * 주문 접수 입력의 상품 라인 — 어떤 상품을 몇 개, 어떤 가격으로, 어떤 옵션으로 주문하는지를 나른다.
 *
 * <p>{@link #selectedOptions()}는 옵션을 고르지 않은 상품이면 {@code null}일 수 있다(기존 동작 보존).
 *
 * <p>{@link #priceId()}는 손님이 고른 가격 행(가격명)이며 <b>{@code null}이면 기본 가격 행</b>을 쓴다 —
 * 가격이 하나뿐인 기존 메뉴의 요청 형태가 그대로 성립해야 하므로 필수로 두지 않는다.
 *
 * <p><b>어느 채널 가격(배달가·픽업가)을 쓸지는 이 값이 정하지 않는다</b> — 서버가 주문유형으로부터
 * 단독 결정한다. 클라이언트가 고르게 하면 픽업가를 주장해 배달을 싸게 사는 우회가 생긴다.
 */
public record OrderPlacementItem(
    Long productId,
    Long priceId,
    Integer quantity,
    List<OrderPlacementItemOption> selectedOptions
) {

    public static OrderPlacementItem of(
        Long productId,
        Long priceId,
        Integer quantity,
        List<OrderPlacementItemOption> selectedOptions
    ) {
        return new OrderPlacementItem(productId, priceId, quantity, selectedOptions);
    }
}

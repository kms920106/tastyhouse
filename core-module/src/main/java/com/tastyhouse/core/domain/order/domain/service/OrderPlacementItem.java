package com.tastyhouse.core.domain.order.domain.service;

import java.util.List;

/**
 * 주문 접수 입력의 상품 라인 — 어떤 상품을 몇 개, 어떤 옵션으로 주문하는지를 나른다.
 *
 * <p>{@link #selectedOptions()}는 옵션을 고르지 않은 상품이면 {@code null}일 수 있다(기존 동작 보존).
 */
public record OrderPlacementItem(
    Long productId,
    Integer quantity,
    List<OrderPlacementItemOption> selectedOptions
) {

    public static OrderPlacementItem of(Long productId, Integer quantity, List<OrderPlacementItemOption> selectedOptions) {
        return new OrderPlacementItem(productId, quantity, selectedOptions);
    }
}

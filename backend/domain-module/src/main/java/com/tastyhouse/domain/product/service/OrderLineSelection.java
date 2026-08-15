package com.tastyhouse.domain.product.service;

import java.util.List;

/**
 * 주문 라인 한 건의 <b>요청</b> — 상품 검증 서비스의 입력이다.
 *
 * <p>order 컨텍스트의 {@code OrderPlacementItem}을 그대로 넘기면 product가 order 내부를 알게 되므로,
 * 검증에 필요한 값만 담은 product 소유 입력 타입을 둔다(호출부가 변환해 넘긴다).
 *
 * <p>선택 옵션이 없으면 {@code selectedOptions}는 빈 목록으로 정규화된다 — 검증 본문에 null 분기를
 * 남기지 않기 위함이다.
 */
public record OrderLineSelection(
    Long productId,
    int quantity,
    List<OrderLineOptionSelection> selectedOptions
) {

    public OrderLineSelection {
        selectedOptions = selectedOptions == null ? List.of() : List.copyOf(selectedOptions);
    }

    public static OrderLineSelection of(
        Long productId,
        int quantity,
        List<OrderLineOptionSelection> selectedOptions
    ) {
        return new OrderLineSelection(productId, quantity, selectedOptions);
    }
}

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
 *
 * <p>{@code priceId}는 손님이 고른 가격 행(가격명)이다. 가격명이 여러 개인 메뉴는 어느 가격을 골랐는지
 * 실려야 하며, <b>{@code null}이면 기본 가격 행({@code sort=0})</b>을 쓴다 — 가격이 하나뿐인 기존
 * 메뉴의 요청 형태가 그대로 성립해야 하기 때문이다.
 *
 * <p><b>어느 채널 가격을 쓸지는 이 값이 정하지 않는다.</b> 그것은 서버가 {@code OrderMethod}로부터
 * 단독 결정한다 — 클라이언트가 고르게 하면 픽업가를 주장해 배달을 싸게 사는 우회가 생긴다.
 */
public record OrderLineSelection(
    Long productId,
    Long priceId,
    int quantity,
    List<OrderLineOptionSelection> selectedOptions
) {

    public OrderLineSelection {
        selectedOptions = selectedOptions == null ? List.of() : List.copyOf(selectedOptions);
    }

    public static OrderLineSelection of(
        Long productId,
        Long priceId,
        int quantity,
        List<OrderLineOptionSelection> selectedOptions
    ) {
        return new OrderLineSelection(productId, priceId, quantity, selectedOptions);
    }
}

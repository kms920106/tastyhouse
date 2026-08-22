package com.tastyhouse.infrastructure.order.query;

import com.querydsl.core.annotations.QueryProjection;

/**
 * 주문 상품 라인의 선택 옵션 조회 결과 — 주문 시점 스냅샷(옵션 그룹명·옵션명·추가 금액)을 투영한다.
 */
public record OrderProductOptionResult(
    Long orderProductId,
    Long orderProductOptionId,
    String optionGroupName,
    String optionName,
    Integer additionalPrice,
    String optionGroupType,
    Integer cupCount,
    Integer depositAmount
) {
    @QueryProjection
    public OrderProductOptionResult {
    }
}

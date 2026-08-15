package com.tastyhouse.domain.product.service;

import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.vo.ProductOptionId;

/**
 * 검증을 통과한 선택 옵션 한 건의 스냅샷.
 *
 * <p>주문 라인 옵션에 <b>주문 당시 값으로 박제</b>되는 이름·추가금을 담는다 — 이후 옵션이 개명되거나
 * 가격이 바뀌어도 과거 주문의 표시가 흔들리지 않게 하기 위함이다.
 */
public record OrderProductOptionSnapshot(
    ProductOptionGroupId optionGroupId,
    String optionGroupName,
    ProductOptionId optionId,
    String optionName,
    int additionalPrice
) {
}

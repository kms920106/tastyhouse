package com.tastyhouse.domain.product.service;

import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.vo.ProductOptionId;

/**
 * 검증을 통과한 선택 옵션 한 건의 스냅샷.
 *
 * <p>주문 라인 옵션에 <b>주문 당시 값으로 박제</b>되는 이름·추가금을 담는다 — 이후 옵션이 개명되거나
 * 가격이 바뀌어도 과거 주문의 표시가 흔들리지 않게 하기 위함이다.
 *
 * <p><b>보증금은 {@code additionalPrice}와 별도 항목으로 담는다.</b> 하나로 합치면 (1) 비과세 신고·정산이
 * 사후에 "이 주문의 보증금이 얼마였나"를 물을 때 분리할 방법이 영구히 사라지고(요율이 그 후 바뀌었을 수
 * 있어 {@code optionId}로 되짚는 것은 답이 아니다), (2) 환급 단위가 컵 개수라 {@code cupCount} 없이는
 * 몇 개 반납분인지 알 수 없으며, (3) 주문상세가 보증금을 별도 줄로 보여줄 수 없다.
 *
 * <p>{@code personalCupDiscountAmount}는 보증금이 아니라 <b>상품 할인 축</b>이다 — 호출부가
 * {@code productDiscountAmount}에 가산해야 총할인 불변식과 최소주문금액 기준이 그대로 유지된다.
 *
 * <p>{@code depositAmount}의 진실원은 <b>옵션 행의 {@code cupCount} × 정책 요율</b>이며 클라이언트 값이
 * 아니다 — 이 스냅샷을 만드는 {@code OrderProductValidationService}가 서버에서 확정한다.
 */
public record OrderProductOptionSnapshot(
    ProductOptionGroupId optionGroupId,
    String optionGroupName,
    ProductOptionId optionId,
    String optionName,
    int additionalPrice,
    String optionGroupType,
    Integer cupCount,
    int depositAmount,
    int personalCupDiscountAmount
) {
}

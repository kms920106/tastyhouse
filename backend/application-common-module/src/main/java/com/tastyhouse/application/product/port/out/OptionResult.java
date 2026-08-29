package com.tastyhouse.application.product.port.out;

/**
 * 옵션 그룹에 속한 개별 옵션 read model.
 *
 * <p>{@code depositAmount}는 저장 컬럼이 아니라 {@code cupCount × 정책 요율}로 계산된 <b>파생 값</b>이다 —
 * 옵션 행에는 개수만 남기기로 한 결정(요율이 바뀌어도 마이그레이션이 없다)의 표시 측 대응이며,
 * 과거 주문의 금액은 주문 스냅샷이 따로 보존한다.
 */
public record OptionResult(
    Long id,
    String name,
    Integer additionalPrice,
    boolean soldOut,
    Integer cupCount,
    Integer depositAmount,
    Integer personalCupDiscountAmount
) {
}

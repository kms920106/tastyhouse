package com.tastyhouse.application.product.port.out;

/**
 * 배치 조회 응답에 포함되는 옵션 read model.
 *
 * <p>보증금 필드가 여기에 있어야 하는 이유는 <b>결제화면 보증금 합계의 원천이 이 배치 조회</b>이기
 * 때문이다 — 빠지면 프론트가 합계를 0으로 계산해 서버 계산값과 어긋나고 주문이
 * {@code ORDER_CUP_DEPOSIT_AMOUNT_MISMATCH}로 거부된다.
 *
 * <p>일반 옵션이면 {@code cupCount}·{@code personalCupDiscountAmount}는 {@code null}이고,
 * {@code depositAmount}는 {@code 0}이다({@code CupDepositPolicy.depositAmountOf}가 컵 개수 없음을
 * 0원으로 계산한다). {@link OptionResult}의 메뉴판 조회와 동일한 형태라 두 화면의 계약이 갈리지 않는다.
 */
public record BatchOptionResult(
    Long id,
    String name,
    Integer price,
    Integer cupCount,
    Integer depositAmount,
    Integer personalCupDiscountAmount
) {
}

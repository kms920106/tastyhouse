package com.tastyhouse.application.product.port.out;

/**
 * 점주 옵션그룹 관리 화면의 개별 옵션 read model.
 *
 * <p>손님용 {@link OptionResult}와 달리 {@code sort}를 함께 담는다 — 관리 화면은 순서를 드래그로
 * 바꾸므로 현재 순서값이 화면에 필요하다.
 *
 * <p>{@code soldOut}은 <b>조작이 아니라 표시</b>를 위한 것이다. 품절·숨김을 바꾸는 화면은 따로 있지만
 * ({@code findProductOptionAvailability}), 합치기 미리보기는 "합치면 무엇이 남는가"를 보여줘야 하므로
 * 지금 팔리지 않는 옵션을 구분해 표시할 수 있어야 한다.
 *
 * @param visible 노출 여부. 삭제(감추기)한 옵션은 {@code false}로 내려온다 — 그룹과 같은 이유로
 *     이 목록은 감춘 옵션도 포함하므로, 화면이 이 값으로 걸러내거나 배지를 붙여야 한다.
 */
public record ProductOptionManagementResult(
    Long id,
    String name,
    Integer additionalPrice,
    Integer sort,
    boolean soldOut,
    boolean visible,
    Integer cupCount,
    Integer personalCupDiscountAmount
) {
}

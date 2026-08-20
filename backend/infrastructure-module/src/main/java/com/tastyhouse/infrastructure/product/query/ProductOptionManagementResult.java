package com.tastyhouse.infrastructure.product.query;

/**
 * 점주 옵션그룹 관리 화면의 개별 옵션 read model.
 *
 * <p>손님용 {@link OptionResult}와 달리 {@code sort}를 함께 담는다 — 관리 화면은 순서를 드래그로
 * 바꾸므로 현재 순서값이 화면에 필요하다. 반대로 품절 여부는 담지 않는다: 품절·숨김 조작은
 * 별도 화면({@code findProductOptionAvailability})의 관심사다.
 *
 * @param visible 노출 여부. 삭제(감추기)한 옵션은 {@code false}로 내려온다 — 그룹과 같은 이유로
 *     이 목록은 감춘 옵션도 포함하므로, 화면이 이 값으로 걸러내거나 배지를 붙여야 한다.
 */
public record ProductOptionManagementResult(
    Long id,
    String name,
    Integer additionalPrice,
    Integer sort,
    boolean visible
) {
}

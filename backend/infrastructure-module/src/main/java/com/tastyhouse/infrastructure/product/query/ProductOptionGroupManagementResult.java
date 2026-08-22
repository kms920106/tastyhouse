package com.tastyhouse.infrastructure.product.query;

import java.util.List;

/**
 * 점주 옵션그룹 관리 화면의 옵션그룹 read model.
 *
 * <p>손님용 {@link OptionGroupResult}와 필드 셋이 달라 통합하지 않는다 — 관리 화면에만 필요한
 * {@code sort}·{@code linkedProductCount}를 손님 응답으로 흘려보내지 않는다.
 *
 * @param sort 이 그룹이 연결된 메뉴 중 하나에서의 순서. 순서는 그룹이 아니라 링크가 갖기 때문에
 *     가게 단위 목록에서는 대표값(가장 작은 값)일 뿐이며, 메뉴별 순서는 메뉴 상세가 따로 조회한다.
 * @param visible 노출 여부. 삭제(감추기)한 그룹은 {@code false}로 내려온다 — 이 목록은 감춘 그룹도
 *     포함하므로(필터를 걸면 되살릴 방법이 없어진다) 화면이 이 값으로 걸러내거나 배지를 붙여야 한다.
 *     이 필드가 없으면 감춘 그룹이 정상 그룹과 구별되지 않아 "삭제했는데 그대로 보인다"가 된다.
 * @param groupType 옵션그룹 유형({@code NORMAL} / {@code CUP_DEPOSIT}). 보증금 그룹은 화면에서 별도
 *     섹션으로 그려지고 컵 개수·개인컵 할인 입력을 노출해야 하므로, 목록 단계에서 구분이 필요하다.
 *     문자열로 담는 이유는 이 record가 표현 계층으로 곧장 흘러가기 때문이다(도메인 enum을 HTTP 경계
 *     밖으로 노출하지 않는다).
 * @param linkedProductCount 이 그룹이 연결된 메뉴 수. 1이면 마지막 연결이라 해제가
 *     {@code PRODUCT_OPTION_GROUP_LAST_LINK_CANNOT_UNLINK}로 거절되므로, 화면이 미리 안내할 수 있다.
 */
public record ProductOptionGroupManagementResult(
    Long id,
    String name,
    String description,
    boolean required,
    boolean multipleSelect,
    Integer minSelect,
    Integer maxSelect,
    Integer sort,
    boolean visible,
    String groupType,
    long linkedProductCount,
    List<ProductOptionManagementResult> options
) {
}

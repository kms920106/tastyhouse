package com.tastyhouse.application.product.port.out;

/**
 * 점주·관리자 메뉴그룹 관리 화면용 read model.
 *
 * <p>손님용 {@link ProductCategoryResult}와 이름이 충돌해 {@code Management} 한정어를 붙였다.
 * 필드 셋이 실제로 다르므로 통합하지 않는다 — 관리 화면에만 필요한 소속 메뉴 수를 손님 응답으로
 * 흘려보내지 않는다.
 *
 * @param productCount 소속된(삭제되지 않은) 메뉴 수. 메뉴그룹 삭제 가능 여부를 화면에서 미리 안내하는 데 쓴다.
 */
public record ProductCategoryManagementResult(
    Long id,
    Long shopId,
    String name,
    String description,
    Integer sort,
    boolean visible,
    long productCount
) {
}

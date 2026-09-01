package com.tastyhouse.application.faq.port.out;

/**
 * FAQ 카테고리 목록 항목(web 노출용) 조회 결과.
 *
 * <p>노출(visible=true) 카테고리만 조회하므로 노출 여부·감사 시각 필드를 갖지 않는다. 관리 목록용
 * 형제인 {@code FaqCategoryManagementResult}와 필드 셋이 달라 통합하지 않는다(과잉 노출 방지).
 */
public record FaqCategoryResult(
    Long id,
    String name,
    Integer sort
) {
}

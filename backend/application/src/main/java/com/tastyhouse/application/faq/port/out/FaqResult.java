package com.tastyhouse.application.faq.port.out;

/**
 * FAQ 항목(web 노출용) 조회 결과.
 *
 * <p>노출(visible=true) 항목만 조회하므로 노출 여부·감사 시각 필드를 갖지 않고, 목록에서 질문과 답변을
 * 함께 펼쳐 보여주므로 answer를 포함한다. 관리 목록용 형제인 {@code FaqManagementListItemResult}와
 * 필드 셋이 달라 통합하지 않는다(과잉 노출 방지).
 */
public record FaqResult(
    Long id,
    Long faqCategoryId,
    String question,
    String answer,
    Integer sort
) {
}

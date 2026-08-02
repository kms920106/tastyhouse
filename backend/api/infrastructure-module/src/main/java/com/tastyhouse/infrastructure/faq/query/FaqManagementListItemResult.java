package com.tastyhouse.infrastructure.faq.query;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

/**
 * FAQ 항목 관리 목록 조회 결과.
 *
 * <p>비노출 항목을 포함해 조회하므로 노출 여부(visible)를 갖고, 목록에서는 답변 본문을 노출하지 않아
 * answer를 갖지 않는다. web 노출용 형제인 {@link FaqResult}와 같은 패키지에 공존해 이름이 충돌하므로
 * 관리 화면 용도를 나타내는 {@code Management} 한정어를 붙였다(과거 core의
 * {@code FaqListItemResult}에 해당).
 */
public record FaqManagementListItemResult(
    Long id,
    Long faqCategoryId,
    String question,
    Integer sort,
    boolean visible,
    LocalDateTime createdAt
) {

    @QueryProjection
    public FaqManagementListItemResult {
    }
}

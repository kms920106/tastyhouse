package com.tastyhouse.application.faq.port.out;

import java.time.LocalDateTime;

/**
 * FAQ 항목 상세 조회 결과.
 *
 * <p>비-admin 형제가 없어 {@code Management} 한정어 없이 순수명을 쓴다. 식별자는 HTTP 경계까지
 * 그대로 전달되는 표현용 값이므로 도메인 VO가 아니라 {@code Long}으로 투영한다(과거 core에서는
 * 도메인 모델을 로드해 조립하며 {@code FaqId}를 담고 있었으나, read 어댑터로 내려오며 직접 투영으로
 * 바뀌었다).
 */
public record FaqDetailResult(
    Long id,
    Long faqCategoryId,
    String question,
    String answer,
    Integer sort,
    boolean visible,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}

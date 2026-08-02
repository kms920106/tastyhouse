package com.tastyhouse.infrastructure.faq.query;

/**
 * FAQ 항목 관리 목록 검색 조건.
 *
 * <p>표현 목적 read의 입력이므로 domain(write 포트)이 아니라 이 query 패키지가 소유한다.
 * 소비 모듈(admin-api)의 {@code FaqQueryService}가 원시 파라미터로 조립해 전달한다.
 */
public record FaqSearchCondition(
    Long categoryId,
    String question,
    Boolean visible
) {

    public static FaqSearchCondition of(Long categoryId, String question, Boolean visible) {
        return new FaqSearchCondition(categoryId, question, visible);
    }
}

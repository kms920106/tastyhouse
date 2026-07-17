package com.tastyhouse.core.domain.faq.application.dto.result;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

public record FaqListItemResult(
    Long id,
    Long faqCategoryId,
    String question,
    Integer sort,
    boolean visible,
    LocalDateTime createdAt
) {
    @QueryProjection
    public FaqListItemResult {
    }
}

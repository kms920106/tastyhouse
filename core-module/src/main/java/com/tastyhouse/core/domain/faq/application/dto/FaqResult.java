package com.tastyhouse.core.domain.faq.application.dto;

import com.querydsl.core.annotations.QueryProjection;

public record FaqResult(
    Long id,
    Long faqCategoryId,
    String question,
    String answer,
    Integer sort
) {
    @QueryProjection
    public FaqResult {
    }
}

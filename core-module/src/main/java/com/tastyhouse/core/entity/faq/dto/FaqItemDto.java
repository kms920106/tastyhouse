package com.tastyhouse.core.entity.faq.dto;

import com.querydsl.core.annotations.QueryProjection;

public record FaqItemDto(
    Long id,
    Long faqCategoryId,
    String question,
    String answer,
    Integer sort
) {
    @QueryProjection
    public FaqItemDto {
    }
}

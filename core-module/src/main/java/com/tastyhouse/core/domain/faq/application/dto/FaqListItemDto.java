package com.tastyhouse.core.domain.faq.application.dto;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

public record FaqListItemDto(
    Long id,
    Long faqCategoryId,
    String question,
    Integer sort,
    boolean visible,
    LocalDateTime createdAt
) {
    @QueryProjection
    public FaqListItemDto {
    }
}

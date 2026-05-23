package com.tastyhouse.core.domain.faq.application.dto;

import com.querydsl.core.annotations.QueryProjection;

public record FaqCategoryResult(
    Long id,
    String name,
    Integer sort
) {
    @QueryProjection
    public FaqCategoryResult {
    }
}

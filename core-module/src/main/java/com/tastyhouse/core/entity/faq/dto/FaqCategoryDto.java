package com.tastyhouse.core.entity.faq.dto;

import com.querydsl.core.annotations.QueryProjection;

public record FaqCategoryDto(
    Long id,
    String name,
    Integer sort
) {
    @QueryProjection
    public FaqCategoryDto {
    }
}

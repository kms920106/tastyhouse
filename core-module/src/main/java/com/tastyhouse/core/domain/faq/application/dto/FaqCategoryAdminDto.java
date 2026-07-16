package com.tastyhouse.core.domain.faq.application.dto;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

public record FaqCategoryAdminDto(
    Long id,
    String name,
    Integer sort,
    boolean visible,
    LocalDateTime createdAt
) {
    @QueryProjection
    public FaqCategoryAdminDto {
    }
}

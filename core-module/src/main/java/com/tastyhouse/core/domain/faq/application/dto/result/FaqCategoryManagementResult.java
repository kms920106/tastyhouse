package com.tastyhouse.core.domain.faq.application.dto.result;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

public record FaqCategoryManagementResult(
    Long id,
    String name,
    Integer sort,
    boolean visible,
    LocalDateTime createdAt
) {
    @QueryProjection
    public FaqCategoryManagementResult {
    }
}

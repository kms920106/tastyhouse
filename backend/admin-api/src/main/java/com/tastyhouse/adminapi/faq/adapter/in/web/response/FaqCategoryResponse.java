package com.tastyhouse.adminapi.faq.adapter.in.web.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "FAQ 카테고리 응답")
public record FaqCategoryResponse(
    @Schema(description = "카테고리 ID", example = "1")
    Long id,

    @Schema(description = "카테고리 이름", example = "결제")
    String name,

    @Schema(description = "정렬 순서", example = "1")
    Integer sort,

    @Schema(description = "노출 여부", example = "true")
    boolean visible,

    @Schema(description = "생성일시", example = "2026-01-01T00:00:00")
    LocalDateTime createdAt
) {
    public static FaqCategoryResponse from(
        Long id,
        String name,
        Integer sort,
        boolean visible,
        LocalDateTime createdAt
    ) {
        return new FaqCategoryResponse(
            id,
            name,
            sort,
            visible,
            createdAt
        );
    }
}

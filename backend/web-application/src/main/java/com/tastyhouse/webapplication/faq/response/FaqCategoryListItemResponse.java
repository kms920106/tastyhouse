package com.tastyhouse.webapplication.faq.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "FAQ 카테고리 목록 항목 응답")
public record FaqCategoryListItemResponse(
    @Schema(description = "FAQ 카테고리 ID", example = "1")
    Long id,

    @Schema(description = "카테고리명", example = "배송/교환/환불")
    String name,

    @Schema(description = "정렬 순서", example = "1")
    Integer sort
) {
    public static FaqCategoryListItemResponse from(
        Long id,
        String name,
        Integer sort
    ) {
        return new FaqCategoryListItemResponse(
            id,
            name,
            sort
        );
    }
}

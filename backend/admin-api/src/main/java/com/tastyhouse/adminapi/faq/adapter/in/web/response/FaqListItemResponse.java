package com.tastyhouse.adminapi.faq.adapter.in.web.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.faq.port.out.FaqManagementListItemResult;

@Schema(description = "FAQ 항목 목록 응답")
public record FaqListItemResponse(
    @Schema(description = "FAQ ID", example = "1")
    Long id,

    @Schema(description = "소속 카테고리 ID", example = "1")
    Long faqCategoryId,

    @Schema(description = "질문", example = "환불은 어떻게 하나요?")
    String question,

    @Schema(description = "정렬 순서", example = "1")
    Integer sort,

    @Schema(description = "노출 여부", example = "true")
    boolean visible,

    @Schema(description = "생성일시", example = "2026-01-01T00:00:00")
    LocalDateTime createdAt
) {
    public static FaqListItemResponse from(FaqManagementListItemResult result) {
        return new FaqListItemResponse(
            result.id(),
            result.faqCategoryId(),
            result.question(),
            result.sort(),
            result.visible(),
            result.createdAt()
        );
    }
}

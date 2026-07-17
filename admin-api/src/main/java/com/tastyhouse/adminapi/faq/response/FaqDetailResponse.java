package com.tastyhouse.adminapi.faq.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.core.domain.faq.application.dto.result.FaqDetailResult;

@Schema(description = "FAQ 항목 상세 응답")
public record FaqDetailResponse(
    @Schema(description = "FAQ ID", example = "1")
    Long id,

    @Schema(description = "소속 카테고리 ID", example = "1")
    Long faqCategoryId,

    @Schema(description = "질문", example = "환불은 어떻게 하나요?")
    String question,

    @Schema(description = "답변", example = "마이페이지 > 주문내역에서 환불 신청이 가능합니다.")
    String answer,

    @Schema(description = "정렬 순서", example = "1")
    Integer sort,

    @Schema(description = "노출 여부", example = "true")
    boolean visible,

    @Schema(description = "생성일시", example = "2026-01-01T00:00:00")
    LocalDateTime createdAt,

    @Schema(description = "수정일시", example = "2026-01-01T00:00:00")
    LocalDateTime updatedAt
) {
    public static FaqDetailResponse from(FaqDetailResult result) {
        return new FaqDetailResponse(
            result.faqId().value(),
            result.faqCategoryId(),
            result.question(),
            result.answer(),
            result.sort(),
            result.visible(),
            result.createdAt(),
            result.updatedAt()
        );
    }
}

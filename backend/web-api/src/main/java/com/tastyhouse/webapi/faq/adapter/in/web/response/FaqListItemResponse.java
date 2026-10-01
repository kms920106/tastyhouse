package com.tastyhouse.webapi.faq.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.faq.port.out.FaqResult;

@Schema(description = "FAQ 목록 항목 응답")
public record FaqListItemResponse(
    @Schema(description = "FAQ ID", example = "1")
    Long id,

    @Schema(description = "FAQ 카테고리 ID", example = "1")
    Long categoryId,

    @Schema(description = "질문", example = "배송은 얼마나 걸리나요?")
    String question,

    @Schema(description = "답변", example = "평균 2~3일 소요됩니다.")
    String answer,

    @Schema(description = "정렬 순서", example = "1")
    Integer sort
) {

    public static FaqListItemResponse from(FaqResult result) {
        return new FaqListItemResponse(
            result.id(),
            result.faqCategoryId(),
            result.question(),
            result.answer(),
            result.sort()
        );
    }
}

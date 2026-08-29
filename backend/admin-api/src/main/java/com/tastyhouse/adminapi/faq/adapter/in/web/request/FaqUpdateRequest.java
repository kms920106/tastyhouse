package com.tastyhouse.adminapi.faq.adapter.in.web.request;

import com.tastyhouse.adminapi.faq.application.port.in.FaqUpdateCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "FAQ 항목 수정 요청")
public record FaqUpdateRequest(
    @NotNull(message = "카테고리 ID는 필수입니다.")
    @Schema(description = "소속 카테고리 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long faqCategoryId,

    @NotBlank(message = "질문은 필수입니다.")
    @Schema(description = "질문", example = "환불은 어떻게 하나요?", requiredMode = Schema.RequiredMode.REQUIRED)
    String question,

    @NotBlank(message = "답변은 필수입니다.")
    @Schema(description = "답변", example = "마이페이지 > 주문내역에서 환불 신청이 가능합니다.", requiredMode = Schema.RequiredMode.REQUIRED)
    String answer,

    @NotNull(message = "정렬 순서는 필수입니다.")
    @Schema(description = "정렬 순서", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer sort,

    @NotNull(message = "노출 여부는 필수입니다.")
    @Schema(description = "노출 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    boolean visible
) {

    public FaqUpdateCommand toCommand(Long faqId) {
        return new FaqUpdateCommand(faqId, faqCategoryId, question, answer, sort, visible);
    }
}

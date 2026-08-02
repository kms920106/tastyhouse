package com.tastyhouse.adminapi.faq.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "FAQ 항목 생성 요청")
public record FaqCreateRequest(
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

    @Schema(description = "노출 여부 (미지정 시 노출)", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    boolean visible
) {
}

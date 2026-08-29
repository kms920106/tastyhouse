package com.tastyhouse.adminapi.faq.adapter.in.web.request;

import com.tastyhouse.adminapi.faq.application.port.in.FaqCategoryUpdateCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "FAQ 카테고리 수정 요청")
public record FaqCategoryUpdateRequest(
    @NotBlank(message = "카테고리 이름은 필수입니다.")
    @Schema(description = "카테고리 이름", example = "결제", requiredMode = Schema.RequiredMode.REQUIRED)
    String name,

    @NotNull(message = "정렬 순서는 필수입니다.")
    @Schema(description = "정렬 순서", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer sort,

    @NotNull(message = "노출 여부는 필수입니다.")
    @Schema(description = "노출 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    boolean visible
) {

    public FaqCategoryUpdateCommand toCommand(Long faqCategoryId) {
        return new FaqCategoryUpdateCommand(faqCategoryId, name, sort, visible);
    }
}

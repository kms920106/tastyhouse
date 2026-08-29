package com.tastyhouse.adminapi.faq.adapter.in.web.request;

import com.tastyhouse.adminapi.faq.application.port.in.FaqCategoryCreateCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "FAQ 카테고리 생성 요청")
public record FaqCategoryCreateRequest(
    @NotBlank(message = "카테고리 이름은 필수입니다.")
    @Schema(description = "카테고리 이름", example = "결제", requiredMode = Schema.RequiredMode.REQUIRED)
    String name,

    @NotNull(message = "정렬 순서는 필수입니다.")
    @Schema(description = "정렬 순서", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer sort,

    @Schema(description = "노출 여부 (미지정 시 노출)", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    boolean visible
) {

    public FaqCategoryCreateCommand toCommand() {
        return new FaqCategoryCreateCommand(name, sort, visible);
    }
}

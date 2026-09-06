package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.application.product.port.in.ProductOptionTargetCommand;

@Schema(description = "옵션 일괄 처리 대상")
public record ProductOptionTargetRequest(
    @NotNull(message = "옵션 ID는 필수입니다.")
    @Schema(description = "대상 옵션 ID", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    Long optionId,

    @NotBlank(message = "옵션 종류는 필수입니다.")
    @Schema(description = "옵션 종류. NORMAL은 일반 옵션, COMMON은 공통 옵션이다.", example = "NORMAL",
        allowableValues = {"NORMAL", "COMMON"}, requiredMode = Schema.RequiredMode.REQUIRED)
    String optionType
) {
    public ProductOptionTargetCommand toCommand() {
        return new ProductOptionTargetCommand(optionId, optionType);
    }
}

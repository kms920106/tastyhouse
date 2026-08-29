package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.ceoapi.product.application.port.in.ProductOptionGroupDeleteCommand;

@Schema(description = "옵션그룹 삭제 요청")
public record ProductOptionGroupDeleteRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "대상 가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId
) {

    public ProductOptionGroupDeleteCommand toCommand(Long ceoId, Long optionGroupId) {
        return new ProductOptionGroupDeleteCommand(ceoId, optionGroupId, shopId);
    }
}

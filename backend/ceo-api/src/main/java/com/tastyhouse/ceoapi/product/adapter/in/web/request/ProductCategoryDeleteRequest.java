package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.application.product.port.in.ProductCategoryDeleteCommand;

@Schema(description = "메뉴그룹 삭제 요청")
public record ProductCategoryDeleteRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "대상 가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId
) {

    public ProductCategoryDeleteCommand toCommand(Long ceoId, Long productCategoryId) {
        return new ProductCategoryDeleteCommand(ceoId, productCategoryId, shopId);
    }
}

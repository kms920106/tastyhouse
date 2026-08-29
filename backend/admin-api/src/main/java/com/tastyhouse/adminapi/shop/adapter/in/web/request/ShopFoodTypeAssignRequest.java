package com.tastyhouse.adminapi.shop.adapter.in.web.request;

import com.tastyhouse.adminapi.shop.application.port.in.ShopFoodTypeAssignCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "가게 음식종류 지정 요청")
public record ShopFoodTypeAssignRequest(
    @NotNull(message = "음식종류 카테고리 ID는 필수입니다.")
    @Schema(description = "음식종류 카테고리 ID", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    Long foodTypeCategoryId
) {

    public ShopFoodTypeAssignCommand toCommand(Long shopId) {
        return new ShopFoodTypeAssignCommand(shopId, foodTypeCategoryId);
    }
}

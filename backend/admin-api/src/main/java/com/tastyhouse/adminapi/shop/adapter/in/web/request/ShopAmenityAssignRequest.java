package com.tastyhouse.adminapi.shop.adapter.in.web.request;

import com.tastyhouse.adminapi.shop.application.port.in.ShopAmenityAssignCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "가게 편의시설 지정 요청")
public record ShopAmenityAssignRequest(
    @NotNull(message = "편의시설 카테고리 ID는 필수입니다.")
    @Schema(description = "편의시설 카테고리 ID", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    Long amenityCategoryId
) {

    public ShopAmenityAssignCommand toCommand(Long adminId, Long shopId) {
        return new ShopAmenityAssignCommand(adminId, shopId, amenityCategoryId);
    }
}

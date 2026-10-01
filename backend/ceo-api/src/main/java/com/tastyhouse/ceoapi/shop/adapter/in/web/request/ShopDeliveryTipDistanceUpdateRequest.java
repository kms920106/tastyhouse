package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.application.shop.port.in.ShopDeliveryTipDistanceUpdateCommand;

@Schema(description = "거리별 추가 배달팁 설정 요청")
public record ShopDeliveryTipDistanceUpdateRequest(
    @NotNull(message = "기본배달거리는 필수입니다.")
    @Min(value = 0, message = "기본배달거리는 0m 이상이어야 합니다.")
    @Schema(description = "기본배달거리(m). 이 거리까지는 할증이 없습니다(1000/1500/2000/2500/3000)", example = "1500", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer baseDistanceMeters,

    @NotBlank(message = "할증 단위는 필수입니다.")
    @Schema(description = "할증 단위", example = "PER_500M", allowableValues = {"PER_100M", "PER_500M"}, requiredMode = Schema.RequiredMode.REQUIRED)
    String surchargeUnit,

    @NotNull(message = "단위당 할증액은 필수입니다.")
    @Min(value = 0, message = "단위당 할증액은 0원 이상이어야 합니다.")
    @Schema(description = "단위당 할증액(원). PER_100M은 100~300원, PER_500M은 100~1,500원", example = "500", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer surchargeAmount
) {

    public ShopDeliveryTipDistanceUpdateCommand toCommand(Long ceoId, Long shopId) {
        return new ShopDeliveryTipDistanceUpdateCommand(ceoId, shopId, baseDistanceMeters(), surchargeUnit(), surchargeAmount());
    }
}

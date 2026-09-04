package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.application.shop.port.in.ShopMinOrderAmountUpdateCommand;

@Schema(description = "가게 최소주문금액 변경 요청")
public record ShopMinOrderAmountUpdateRequest(
    @NotNull(message = "최소주문금액은 필수입니다.")
    @Min(value = 0, message = "최소주문금액은 0원 이상이어야 합니다.")
    @Schema(
        description = "최소주문금액 (0: 미설정, 설정 시 5000~30000). 배달 주문에만 적용됩니다.",
        example = "10000",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    Integer minOrderAmount
) {

    public ShopMinOrderAmountUpdateCommand toCommand(Long ceoId, Long shopId) {
        return new ShopMinOrderAmountUpdateCommand(ceoId, shopId, minOrderAmount());
    }
}

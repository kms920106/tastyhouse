package com.tastyhouse.ceoapi.shop.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "구간별 기본 배달팁 한 구간")
public record ShopDeliveryTipTierItemRequest(
    @NotNull(message = "구간 최소주문금액은 필수입니다.")
    @Min(value = 0, message = "구간 최소주문금액은 0원 이상이어야 합니다.")
    @Schema(description = "이 구간이 적용되는 최소주문금액(원)", example = "5000", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer minOrderAmount,

    @NotNull(message = "배달팁은 필수입니다.")
    @Min(value = 0, message = "배달팁은 0원 이상이어야 합니다.")
    @Schema(description = "이 구간의 배달팁(원). 5,000원 미만", example = "2000", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer tipAmount
) {
}

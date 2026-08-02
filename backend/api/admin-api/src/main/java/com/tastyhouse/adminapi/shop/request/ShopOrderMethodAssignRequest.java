package com.tastyhouse.adminapi.shop.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "가게 주문수단 지정 요청")
public record ShopOrderMethodAssignRequest(
    @NotBlank(message = "주문수단은 필수입니다.")
    @Schema(description = "주문수단", example = "TABLE", allowableValues = {"TABLE", "RESERVATION", "DELIVERY", "TAKEOUT"}, requiredMode = Schema.RequiredMode.REQUIRED)
    String orderMethod
) {
}

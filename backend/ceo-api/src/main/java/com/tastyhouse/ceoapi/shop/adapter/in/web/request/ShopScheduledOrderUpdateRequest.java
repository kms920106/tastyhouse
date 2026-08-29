package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.ceoapi.shop.application.port.in.ShopScheduledOrderUpdateCommand;

@Schema(description = "가게 예약주문 운영 여부 변경 요청")
public record ShopScheduledOrderUpdateRequest(
    @NotNull(message = "예약주문 운영 여부는 필수입니다.")
    @Schema(
        description = "예약주문 운영 여부 (true: 설정함, false: 설정안함). 끄더라도 이미 접수된 예약주문은 유지됩니다.",
        example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    Boolean enabled
) {

    public ShopScheduledOrderUpdateCommand toCommand(Long ceoId, Long shopId) {
        return new ShopScheduledOrderUpdateCommand(ceoId, shopId, enabled());
    }
}

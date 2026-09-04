package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.application.shop.port.in.ShopDeliveryTipHolidayUpdateCommand;

@Schema(description = "공휴일 추가 배달팁 설정 요청")
public record ShopDeliveryTipHolidayUpdateRequest(
    @NotNull(message = "배달팁은 필수입니다.")
    @Min(value = 0, message = "배달팁은 0원 이상이어야 합니다.")
    @Schema(description = "법정 공휴일에 부과할 추가 배달팁(원). 0원을 보내면 설정이 삭제됩니다", example = "2000", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer tipAmount
) {

    public ShopDeliveryTipHolidayUpdateCommand toCommand(Long ceoId, Long shopId) {
        return new ShopDeliveryTipHolidayUpdateCommand(ceoId, shopId, tipAmount());
    }
}

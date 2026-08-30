package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import com.tastyhouse.ceoapplication.shop.port.in.ShopClosedDayCreateCommand;

@Schema(description = "가게 정기 휴무일 등록 요청")
public record ShopClosedDayCreateRequest(
    @NotBlank(message = "정기 휴무 유형은 필수입니다.")
    @Schema(description = "정기 휴무 유형", example = "EVERY_WEEK_MONDAY", requiredMode = Schema.RequiredMode.REQUIRED)
    String closedDayType
) {

    public ShopClosedDayCreateCommand toCommand(Long ceoId, Long shopId) {
        return new ShopClosedDayCreateCommand(ceoId, shopId, closedDayType());
    }
}

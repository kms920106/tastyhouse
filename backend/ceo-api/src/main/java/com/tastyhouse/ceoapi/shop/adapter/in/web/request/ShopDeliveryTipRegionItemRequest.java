package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.ceoapplication.shop.port.in.ShopDeliveryTipRegionCommand;

@Schema(description = "지역별 추가 배달팁 한 건")
public record ShopDeliveryTipRegionItemRequest(
    @NotNull(message = "행정동 ID는 필수입니다.")
    @Schema(description = "행정동 ID. 가게의 배달가능지역으로 등록된 행정동이어야 합니다", example = "1101053", requiredMode = Schema.RequiredMode.REQUIRED)
    Long adminDongId,

    @NotNull(message = "배달팁은 필수입니다.")
    @Min(value = 0, message = "배달팁은 0원 이상이어야 합니다.")
    @Schema(description = "이 행정동의 추가 배달팁(원). 10,000원 이하", example = "1000", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer tipAmount
) {

    public ShopDeliveryTipRegionCommand toCommand() {
        return new ShopDeliveryTipRegionCommand(adminDongId(), tipAmount());
    }
}

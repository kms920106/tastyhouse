package com.tastyhouse.adminapi.shop.adapter.in.web.request;

import com.tastyhouse.application.shop.port.in.ShopCupDepositChangeCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "일회용컵 보증금제 대상 사업자 지정/해제 요청")
public record ShopCupDepositRequest(
    @NotNull(message = "대상 여부는 필수입니다.")
    @Schema(description = "대상 사업자 여부. true면 이 가게가 보증금 옵션그룹을 만들 수 있습니다.",
        example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean enabled
) {

    public ShopCupDepositChangeCommand toCommand(Long shopId) {
        return new ShopCupDepositChangeCommand(shopId, enabled);
    }
}

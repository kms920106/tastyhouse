package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.application.shop.port.in.ShopDeliveryTipTierCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryTipTiersUpdateCommand;

@Schema(description = "구간별 기본 배달팁 일괄 교체 요청")
public record ShopDeliveryTipTiersUpdateRequest(
    @NotNull(message = "구간 목록은 필수입니다.")
    @Valid
    @Schema(description = "구간 목록(1~3개, 주문금액 오름차순·배달팁 내림차순)", requiredMode = Schema.RequiredMode.REQUIRED)
    List<ShopDeliveryTipTierItemRequest> tiers
) {
    public ShopDeliveryTipTiersUpdateCommand toCommand(Long ceoId, Long shopId) {
        List<ShopDeliveryTipTierCommand> tierCommands = tiers().stream()
            .map(ShopDeliveryTipTierItemRequest::toCommand)
            .toList();
        return new ShopDeliveryTipTiersUpdateCommand(ceoId, shopId, tierCommands);
    }
}

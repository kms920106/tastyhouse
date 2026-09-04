package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.application.shop.port.in.ShopDeliveryTipRegionCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryTipRegionsUpdateCommand;

/**
 * 지역별 추가 배달팁 일괄 교체 요청.
 *
 * <p>빈 배열은 "전부 삭제"를 뜻하는 정상 입력이므로 {@code @NotEmpty}를 쓰지 않는다 — 지역별을 전부
 * 지워야 거리별로 전환할 수 있는 규격이라 빈 배열이 실제 사용되는 경로다.
 */
@Schema(description = "지역별 추가 배달팁 일괄 교체 요청")
public record ShopDeliveryTipRegionsUpdateRequest(
    @NotNull(message = "지역 목록은 필수입니다.")
    @Valid
    @Schema(description = "지역별 배달팁 목록. 빈 배열이면 전부 삭제됩니다", requiredMode = Schema.RequiredMode.REQUIRED)
    List<ShopDeliveryTipRegionItemRequest> regions
) {

    public ShopDeliveryTipRegionsUpdateCommand toCommand(Long ceoId, Long shopId) {
        List<ShopDeliveryTipRegionCommand> regionCommands = regions().stream()
            .map(ShopDeliveryTipRegionItemRequest::toCommand)
            .toList();
        return new ShopDeliveryTipRegionsUpdateCommand(ceoId, shopId, regionCommands);
    }
}

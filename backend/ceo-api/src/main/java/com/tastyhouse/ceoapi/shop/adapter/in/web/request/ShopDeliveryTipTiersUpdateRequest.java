package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.ceoapplication.shop.port.in.ShopDeliveryTipTierCommand;
import com.tastyhouse.ceoapplication.shop.port.in.ShopDeliveryTipTiersUpdateCommand;

/**
 * 구간별 기본 배달팁 일괄 교체 요청.
 *
 * <p>{@code tiers}에 {@code @NotEmpty}를 붙이지 않고 {@code @NotNull}만 두는 것은 의도다 — "1~3개"라는
 * 개수 불변식은 도메인 서비스가 {@code SHOP_DELIVERY_TIP_TIER_LIMIT_EXCEEDED}로 판정하므로, 여기서
 * 빈 배열만 따로 400 검증 오류로 가로채면 같은 위반(개수 규칙)이 입력값에 따라 서로 다른 에러코드로
 * 내려가 프론트 분기가 갈린다.
 */
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

package com.tastyhouse.webapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopPriceBadgeViewResult;

@Schema(description = "가게 매장가격 뱃지 노출 여부")
public record ShopPriceBadgeResponse(
    @Schema(description = "'매장과 같은 가격' 뱃지 노출 여부. 가게의 매장가격 인증이 켜져 있으면 true입니다.",
        example = "true")
    boolean sameAsStorePrice,

    @Schema(description = "'매장가격 픽업' 뱃지 노출 여부. 픽업가가 매장가 이하이고 전체 메뉴의 80% 이상이 "
        + "매장가·픽업가를 가지며, 픽업가 설정 익일(영업일)이 지났을 때 true입니다.",
        example = "false")
    boolean storePricePickup
) {
    public static ShopPriceBadgeResponse from(ShopPriceBadgeViewResult result) {
        return new ShopPriceBadgeResponse(result.sameAsStorePrice(), result.storePricePickup());
    }
}

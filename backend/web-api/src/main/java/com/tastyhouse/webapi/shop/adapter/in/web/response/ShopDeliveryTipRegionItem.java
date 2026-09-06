package com.tastyhouse.webapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopDeliveryTipRegionResult;

@Schema(description = "지역별 추가 배달팁")
public record ShopDeliveryTipRegionItem(
    @Schema(description = "행정동 전체 이름", example = "서울특별시 강남구 역삼1동")
    String regionName,

    @Schema(description = "이 지역의 추가 배달팁(원)", example = "1000")
    int tipAmount
) {
    public static ShopDeliveryTipRegionItem from(ShopDeliveryTipRegionResult result) {
        return new ShopDeliveryTipRegionItem(
            result.regionName(),
            result.tipAmount()
        );
    }
}

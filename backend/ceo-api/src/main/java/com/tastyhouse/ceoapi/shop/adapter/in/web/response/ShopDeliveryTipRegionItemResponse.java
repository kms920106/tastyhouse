package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopDeliveryTipRegionResult;

@Schema(description = "지역별 추가 배달팁 한 건")
public record ShopDeliveryTipRegionItemResponse(
    @Schema(description = "지역별 배달팁 ID", example = "10")
    long id,

    @Schema(description = "행정동 ID", example = "1101053")
    long adminDongId,

    @Schema(description = "행정동 전체 이름", example = "서울특별시 강남구 역삼1동")
    String regionName,

    @Schema(description = "이 행정동의 추가 배달팁(원)", example = "1000")
    int tipAmount
) {
    public static ShopDeliveryTipRegionItemResponse from(ShopDeliveryTipRegionResult result) {
        return new ShopDeliveryTipRegionItemResponse(
            result.id(),
            result.adminDongId(),
            result.regionName(),
            result.tipAmount()
        );
    }
}

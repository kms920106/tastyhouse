package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

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
    public static ShopDeliveryTipRegionItemResponse from(
        long id,
        long adminDongId,
        String regionName,
        int tipAmount
    ) {
        return new ShopDeliveryTipRegionItemResponse(
            id,
            adminDongId,
            regionName,
            tipAmount
        );
    }
}

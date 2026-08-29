package com.tastyhouse.webapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 지역별 추가 배달팁 한 행.
 *
 * <p>{@code regionName}은 서버가 행정동 마스터를 조인해 완성한 전체 이름이다 — 프론트가 시도·시군구·
 * 동을 조립하지 않는다.
 */
@Schema(description = "지역별 추가 배달팁")
public record ShopDeliveryTipRegionItem(
    @Schema(description = "행정동 전체 이름", example = "서울특별시 강남구 역삼1동")
    String regionName,

    @Schema(description = "이 지역의 추가 배달팁(원)", example = "1000")
    int tipAmount
) {
    public static ShopDeliveryTipRegionItem from(
        String regionName,
        int tipAmount
    ) {
        return new ShopDeliveryTipRegionItem(
            regionName,
            tipAmount
        );
    }
}

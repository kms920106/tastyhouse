package com.tastyhouse.ceoapi.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 배달가능지역 한 건")
public record ShopDeliveryAreaItemResponse(
    @Schema(description = "배달가능지역 ID", example = "10")
    long id,

    @Schema(description = "행정동 ID", example = "1101053")
    long adminDongId,

    @Schema(description = "행정동 전체 이름", example = "서울특별시 강남구 역삼1동")
    String regionName
) {
    public static ShopDeliveryAreaItemResponse from(
        long id,
        long adminDongId,
        String regionName
    ) {
        return new ShopDeliveryAreaItemResponse(
            id,
            adminDongId,
            regionName
        );
    }
}

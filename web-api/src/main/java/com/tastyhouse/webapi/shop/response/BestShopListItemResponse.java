package com.tastyhouse.webapi.shop.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "베스트 가게 목록 항목 응답")
public record BestShopListItemResponse(
    @Schema(description = "가게 ID", example = "1")
    Long id,

    @Schema(description = "가게명", example = "BBQ치킨 성내점")
    String name,

    @Schema(description = "인접 지하철역명", example = "강남역")
    String stationName,

    @Schema(description = "평점", example = "4.5")
    Double rating,

    @Schema(description = "가게 썸네일 이미지 URL", example = "https://cdn.tastyhouse.com/shop/1/thumbnail.jpg")
    String imageUrl,

    @Schema(description = "음식 종류 목록")
    List<String> foodTypes
) {
    public static BestShopListItemResponse from(
        Long id,
        String name,
        String stationName,
        Double rating,
        String imageUrl,
        List<String> foodTypes
    ) {
        return new BestShopListItemResponse(
            id,
            name,
            stationName,
            rating,
            imageUrl,
            foodTypes
        );
    }
}

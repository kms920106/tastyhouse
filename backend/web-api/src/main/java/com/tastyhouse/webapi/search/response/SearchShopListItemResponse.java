package com.tastyhouse.webapi.search.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "매장 검색 목록 아이템 응답")
public record SearchShopListItemResponse(
    @Schema(description = "매장 ID", example = "1")
    Long shopId,

    @Schema(description = "북마크 ID (북마크하지 않은 경우 null)", example = "1")
    Long bookmarkId,

    @Schema(description = "매장명", example = "맛있는 마라탕집")
    String shopName,

    @Schema(description = "인근 역 이름", example = "강남역")
    String stationName,

    @Schema(description = "매장 평점", example = "4.5")
    Double rating,

    @Schema(description = "매장 대표 이미지 URL", example = "https://cdn.tastyhouse.com/shop/1.jpg")
    String imageUrl,

    @Schema(description = "북마크 여부", example = "false")
    boolean bookmarked,

    @Schema(description = "최소주문금액 (0: 미설정, 제한 없음). 배달 주문에만 적용됩니다.", example = "10000")
    int minOrderAmount
) {
    public static SearchShopListItemResponse from(
        Long shopId,
        String shopName,
        String stationName,
        Double rating,
        String imageUrl,
        boolean bookmarked,
        int minOrderAmount
    ) {
        return new SearchShopListItemResponse(
            shopId,
            null,
            shopName,
            stationName,
            rating,
            imageUrl,
            bookmarked,
            minOrderAmount
        );
    }
}

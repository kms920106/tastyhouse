package com.tastyhouse.webapi.member.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "찜한 매장 목록 아이템")
public record ShopBookmarkListItemResponse(
    @Schema(description = "매장 ID(PK)", example = "1")
    Long shopId,

    @Schema(description = "북마크 ID(PK)", example = "1")
    Long bookmarkId,

    @Schema(description = "매장명", example = "맛있는 김밥집")
    String shopName,

    @Schema(description = "인접 지하철역명", example = "강남역")
    String stationName,

    @Schema(description = "평점", example = "4.5")
    Double rating,

    @Schema(description = "매장 이미지 URL", example = "https://cdn.tastyhouse.com/shop/image/1.jpg")
    String imageUrl,

    @Schema(description = "북마크(찜) 여부", example = "true")
    boolean bookmarked
) {
    public static ShopBookmarkListItemResponse from(
        Long shopId,
        Long bookmarkId,
        String shopName,
        String stationName,
        Double rating,
        String imageUrl,
        boolean bookmarked
    ) {
        return new ShopBookmarkListItemResponse(
            shopId,
            bookmarkId,
            shopName,
            stationName,
            rating,
            imageUrl,
            bookmarked
        );
    }
}

package com.tastyhouse.webapi.member.response;

public record ShopBookmarkListItemResponse(
    Long shopId,
    Long bookmarkId,
    String shopName,
    String stationName,
    Double rating,
    String imageUrl,
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

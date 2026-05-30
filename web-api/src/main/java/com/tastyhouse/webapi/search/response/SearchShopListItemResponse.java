package com.tastyhouse.webapi.search.response;

public record SearchShopListItemResponse(
    Long shopId,
    Long bookmarkId,
    String shopName,
    String stationName,
    Double rating,
    String imageUrl,
    boolean bookmarked
) {
    public static SearchShopListItemResponse from(
        Long shopId,
        String shopName,
        String stationName,
        Double rating,
        String imageUrl,
        boolean bookmarked
    ) {
        return new SearchShopListItemResponse(
            shopId,
            null,
            shopName,
            stationName,
            rating,
            imageUrl,
            bookmarked
        );
    }
}

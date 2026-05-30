package com.tastyhouse.webapi.review.response;

public record BestReviewListItemResponse(
    Long id,
    String imageUrl,
    String stationName,
    String shopName,
    String productName,
    Double totalRating,
    String content
) {
    public static BestReviewListItemResponse from(
        Long id,
        String imageUrl,
        String stationName,
        String shopName,
        String productName,
        Double totalRating,
        String content
    ) {
        return new BestReviewListItemResponse(
            id,
            imageUrl,
            stationName,
            shopName,
            productName,
            totalRating,
            content
        );
    }
}

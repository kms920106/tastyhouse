package com.tastyhouse.webapi.review.response;

public record BestReviewListItem(
    Long id,
    String imageUrl,
    String stationName,
    String placeName,
    String productName,
    Double totalRating,
    String content
) {
    public static BestReviewListItem from(
        Long id,
        String imageUrl,
        String stationName,
        String placeName,
        String productName,
        Double totalRating,
        String content
    ) {
        return new BestReviewListItem(
            id,
            imageUrl,
            stationName,
            placeName,
            productName,
            totalRating,
            content
        );
    }
}

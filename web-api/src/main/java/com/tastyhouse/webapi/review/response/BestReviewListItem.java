package com.tastyhouse.webapi.review.response;

public record BestReviewListItem(
    Long id,
    String imageUrl,
    String stationName,
    Double totalRating,
    String content
) {
    public static BestReviewListItem from(
        Long id,
        String imageUrl,
        String stationName,
        Double totalRating,
        String content
    ) {
        return new BestReviewListItem(
            id,
            imageUrl,
            stationName,
            totalRating,
            content
        );
    }
}

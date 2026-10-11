package com.tastyhouse.application.review.port.out;

public record BestReviewListItemResult(
    Long id,
    String imageUrl,
    String stationName,
    String shopName,
    String productName,
    Double totalRating,
    String content
) {
}

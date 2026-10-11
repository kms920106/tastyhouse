package com.tastyhouse.application.review.port.out;

public record ShopReviewMonthlyStatResult(
    String yearMonth,
    Double averageRating,
    Long reviewCount
) {
}

package com.tastyhouse.application.review.port.out;

public record ShopReviewCategoryAverageResult(
    Double tasteRating,
    Double amountRating,
    Double priceRating,
    Double atmosphereRating,
    Double kindnessRating,
    Double hygieneRating
) {
}

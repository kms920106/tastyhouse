package com.tastyhouse.webapi.product.response;

public record ProductReviewsByRatingWithPagination(
    ProductReviewsByRatingResponse response,
    long totalElements
) {
}

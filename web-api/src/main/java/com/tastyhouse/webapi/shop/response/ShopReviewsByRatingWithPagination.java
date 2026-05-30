package com.tastyhouse.webapi.shop.response;

public record ShopReviewsByRatingWithPagination(
    ShopReviewsByRatingResponse response,
    long totalElements
) {
}

package com.tastyhouse.webapi.member.response;

public record MyReviewListItemResponse(
    Long id,
    String imageUrl
) {
    public static MyReviewListItemResponse from(Long reviewId, String imageUrl) {
    return new MyReviewListItemResponse(reviewId, imageUrl);
    }
}

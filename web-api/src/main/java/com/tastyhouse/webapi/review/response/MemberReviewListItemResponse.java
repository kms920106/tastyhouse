package com.tastyhouse.webapi.review.response;

public record MemberReviewListItemResponse(
    Long id,
    String imageUrl
) {
    public static MemberReviewListItemResponse from(Long reviewId, String imageUrl) {
        return new MemberReviewListItemResponse(
            reviewId,
            imageUrl
        );
    }
}

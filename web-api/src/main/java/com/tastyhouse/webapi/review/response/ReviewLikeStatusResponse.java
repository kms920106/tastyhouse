package com.tastyhouse.webapi.review.response;

public record ReviewLikeStatusResponse(
    Boolean liked
) {
    public static ReviewLikeStatusResponse from(Boolean liked) {
        return new ReviewLikeStatusResponse(liked);
    }
}

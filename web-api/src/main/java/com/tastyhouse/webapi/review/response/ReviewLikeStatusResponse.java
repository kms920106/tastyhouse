package com.tastyhouse.webapi.review.response;

public record ReviewLikeStatusResponse(
    boolean liked
) {
    public static ReviewLikeStatusResponse from(boolean liked) {
        return new ReviewLikeStatusResponse(liked);
    }
}

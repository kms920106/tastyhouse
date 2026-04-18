package com.tastyhouse.webapi.review.response;

public record ReviewLikeResponse(
    boolean liked
) {
    public static ReviewLikeResponse from(boolean liked) {
    return new ReviewLikeResponse(liked);
    }
}

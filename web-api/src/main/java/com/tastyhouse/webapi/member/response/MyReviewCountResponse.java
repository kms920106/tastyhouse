package com.tastyhouse.webapi.member.response;

public record MyReviewCountResponse(
    long reviewCount
) {
    public static MyReviewCountResponse from(long reviewCount) {
        return new MyReviewCountResponse(reviewCount);
    }
}

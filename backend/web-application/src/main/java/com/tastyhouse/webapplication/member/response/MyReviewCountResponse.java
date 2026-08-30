package com.tastyhouse.webapplication.member.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 리뷰 개수")
public record MyReviewCountResponse(
    @Schema(description = "작성한 리뷰 개수", example = "12")
    long reviewCount
) {
    public static MyReviewCountResponse from(long reviewCount) {
        return new MyReviewCountResponse(reviewCount);
    }
}

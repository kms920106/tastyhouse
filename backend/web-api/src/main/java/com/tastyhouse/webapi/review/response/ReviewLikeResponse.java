package com.tastyhouse.webapi.review.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "리뷰 좋아요 처리 응답")
public record ReviewLikeResponse(
    @Schema(description = "좋아요 여부", example = "true")
    boolean liked
) {
    public static ReviewLikeResponse from(boolean liked) {
        return new ReviewLikeResponse(liked);
    }
}

package com.tastyhouse.webapi.review.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "리뷰 좋아요 상태 응답")
public record ReviewLikeStatusResponse(
    @Schema(description = "좋아요 여부", example = "true")
    boolean liked
) {
    public static ReviewLikeStatusResponse from(boolean liked) {
        return new ReviewLikeStatusResponse(liked);
    }
}

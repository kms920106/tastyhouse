package com.tastyhouse.webapi.member.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 리뷰 목록 아이템")
public record MyReviewListItemResponse(
    @Schema(description = "리뷰 ID(PK)", example = "1")
    Long id,

    @Schema(description = "리뷰 이미지 URL", example = "https://cdn.tastyhouse.com/review/image/1.jpg")
    String imageUrl
) {
    public static MyReviewListItemResponse from(
        Long reviewId,
        String imageUrl
    ) {
        return new MyReviewListItemResponse(reviewId, imageUrl);
    }
}

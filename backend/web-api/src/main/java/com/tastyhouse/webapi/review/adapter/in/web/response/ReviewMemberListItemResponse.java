package com.tastyhouse.webapi.review.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 리뷰 목록 항목 응답")
public record ReviewMemberListItemResponse(
    @Schema(description = "리뷰 ID", example = "1")
    Long id,

    @Schema(description = "리뷰 이미지 URL", example = "https://cdn.tastyhouse.com/review/1/image.jpg")
    String imageUrl
) {
    public static ReviewMemberListItemResponse from(
        Long reviewId,
        String imageUrl
    ) {
        return new ReviewMemberListItemResponse(
            reviewId,
            imageUrl
        );
    }
}

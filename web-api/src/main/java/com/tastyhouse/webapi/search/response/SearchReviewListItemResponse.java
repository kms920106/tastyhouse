package com.tastyhouse.webapi.search.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "리뷰 검색 목록 아이템 응답")
public record SearchReviewListItemResponse(
    @Schema(description = "리뷰 ID", example = "1")
    Long id,

    @Schema(description = "리뷰 이미지 URL", example = "https://cdn.tastyhouse.com/review/1.jpg")
    String imageUrl
) {
    public static SearchReviewListItemResponse from(Long id, String imageUrl) {
        return new SearchReviewListItemResponse(id, imageUrl);
    }
}

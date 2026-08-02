package com.tastyhouse.adminapi.review.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "리뷰 검색 요청")
public record ReviewSearchRequest(
    @Schema(description = "상점 ID", example = "1")
    Long shopId,

    @Schema(description = "상품 ID", example = "1")
    Long productId,

    @Schema(description = "작성 회원 ID", example = "1")
    Long memberId,

    @Schema(description = "숨김 여부 (null=전체, true=숨김만, false=노출만)", example = "false")
    Boolean hidden,

    @Schema(description = "내용 (부분 일치 검색)", example = "맛있어요")
    String content,

    @Schema(description = "최소 평점", example = "1.0")
    Double minRating,

    @Schema(description = "최대 평점", example = "5.0")
    Double maxRating
) {
}

package com.tastyhouse.webapi.shop.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 리뷰 조회 요청")
public record ShopReviewSearchRequest(
    @Schema(description = "이미지 유무 필터: 미지정=전체, true=이미지 있는 리뷰, false=이미지 없는 리뷰", example = "true")
    Boolean hasImage
) {
}

package com.tastyhouse.webapi.shop.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 리뷰 조회 요청")
public record ShopReviewSearchRequest(
    @Schema(description = "이미지 유무 필터: 미지정=전체, true=이미지 있는 리뷰, false=이미지 없는 리뷰", example = "true")
    Boolean hasImage,

    @Schema(
        description = "정렬 방식. 미지정 시 점주가 저장한 기본 정렬을 적용하며, 그 설정도 없으면 최신순입니다. "
            + "명시하면 점주 설정보다 이 값이 우선합니다.",
        allowableValues = {"RECOMMENDED", "LATEST", "OLDEST"},
        example = "LATEST"
    )
    String sortType
) {
}

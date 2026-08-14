package com.tastyhouse.ceoapi.review.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 리뷰 정렬 설정 저장 요청.
 */
@Schema(description = "리뷰 정렬 설정 저장 요청")
public record ShopReviewSortTypeUpdateRequest(
    @NotBlank(message = "정렬 방식은 필수입니다.")
    @Schema(
        description = "고객 앱 리뷰 목록에 적용할 기본 정렬",
        allowableValues = {"RECOMMENDED", "LATEST", "OLDEST"},
        example = "RECOMMENDED",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String sortType
) {
}

package com.tastyhouse.adminapi.shop.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "배달지역 조정 신청 검색 요청")
public record ShopDeliveryAreaAdjustmentSearchRequest(
    @Schema(description = "처리 상태. 미지정이면 전체", example = "PENDING", allowableValues = {"PENDING", "IN_PROGRESS", "COMPLETED", "REJECTED"})
    String status,

    @Schema(description = "가게 ID 필터. 미지정이면 전체", example = "1")
    Long shopId
) {
}

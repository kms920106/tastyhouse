package com.tastyhouse.adminapi.shop.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 이미지 변경 요청 검색 요청")
public record ShopImageChangeRequestSearchRequest(
    @Schema(description = "승인 상태", example = "PENDING", allowableValues = {"PENDING", "APPROVED", "REJECTED"})
    String status,

    @Schema(description = "이미지 유형", example = "TRADEMARK", allowableValues = {"TRADEMARK", "THUMBNAIL"})
    String imageType
) {
}

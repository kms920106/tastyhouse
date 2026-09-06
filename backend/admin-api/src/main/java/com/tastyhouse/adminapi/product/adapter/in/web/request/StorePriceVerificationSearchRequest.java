package com.tastyhouse.adminapi.product.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "매장 가격 인증 요청 검수 목록 검색 조건")
public record StorePriceVerificationSearchRequest(
    @Schema(description = "인증 요청 상태. 지정하지 않으면 전체", example = "PENDING",
        allowableValues = {"PENDING", "IN_PROGRESS", "APPROVED", "REJECTED", "CANCELED"})
    String status
) {
}

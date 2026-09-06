package com.tastyhouse.adminapi.product.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메뉴 승인요청 검수 목록 검색 조건")
public record ProductApprovalSearchRequest(
    @Schema(description = "승인 상태. 지정하지 않으면 전체", example = "PENDING",
        allowableValues = {"PENDING", "APPROVED", "REJECTED", "CANCELED"})
    String status
) {
}

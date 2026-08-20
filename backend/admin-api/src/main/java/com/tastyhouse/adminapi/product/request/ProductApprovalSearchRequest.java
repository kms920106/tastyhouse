package com.tastyhouse.adminapi.product.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 메뉴 이미지·채식 승인요청 검수 목록 검색 조건.
 *
 * <p>{@code status}는 도메인 enum 경계 규칙에 따라 {@code String}으로 받고 Service가 승격한다.
 * 지정하지 않으면 상태 무관 전체를 조회한다.
 */
@Schema(description = "메뉴 승인요청 검수 목록 검색 조건")
public record ProductApprovalSearchRequest(
    @Schema(description = "승인 상태. 지정하지 않으면 전체", example = "PENDING",
        allowableValues = {"PENDING", "APPROVED", "REJECTED", "CANCELED"})
    String status
) {
}

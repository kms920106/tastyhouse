package com.tastyhouse.adminapi.product.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 매장 가격 인증 요청 검수 목록 검색 조건.
 *
 * <p>{@code status}는 도메인 enum 경계 규칙에 따라 {@code String}으로 받고 Service가 승격한다.
 * 지정하지 않으면 상태 무관 전체를 조회한다.
 *
 * <p>{@code ProductApprovalSearchRequest}를 재사용하지 않는 이유는 <b>상태 집합이 다르기 때문이다</b> —
 * 인증 요청은 {@code IN_PROGRESS}(검수 착수)를 갖는다. 한 record를 공유하면 Swagger의
 * {@code allowableValues}가 어느 한쪽에 대해 거짓말을 하게 된다.
 */
@Schema(description = "매장 가격 인증 요청 검수 목록 검색 조건")
public record StorePriceVerificationSearchRequest(
    @Schema(description = "인증 요청 상태. 지정하지 않으면 전체", example = "PENDING",
        allowableValues = {"PENDING", "IN_PROGRESS", "APPROVED", "REJECTED", "CANCELED"})
    String status
) {
}

package com.tastyhouse.ceoapi.product.request;

import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 가게 소유권 검증만 필요한 조회 조건.
 *
 * <p>{@code shopId}를 경로가 아니라 query로 받는다 — 경로에 가게 식별자가 없으면 검증을 생략하기
 * 쉽고, 이 저장소는 그 형태로 IDOR을 낸 전례가 있다.
 */
@Schema(description = "가게 범위 조회 조건")
public record ProductShopScopeRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "대상 가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId
) {
}

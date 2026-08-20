package com.tastyhouse.ceoapi.product.request;

import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 메뉴-옵션그룹 연결·해제 요청. 메뉴 id와 옵션그룹 id는 경로로 받고 가게 id만 본문·query로 받는다.
 */
@Schema(description = "메뉴-옵션그룹 연결·해제 요청")
public record ProductOptionGroupLinkRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "대상 가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId
) {
}

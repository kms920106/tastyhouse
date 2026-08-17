package com.tastyhouse.ceoapi.product.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "품절·숨김 관리 목록 조회 조건")
public record ProductAvailabilitySearchRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "대상 가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @Size(max = 100, message = "검색어는 100자 이하여야 합니다.")
    @Schema(description = "검색어. 메뉴 탭은 메뉴명, 옵션 탭은 옵션명에 부분일치(대소문자 무시)한다.", example = "떡볶이")
    String keyword,

    @Schema(description = "품절 항목만 보기. hiddenOnly와 함께 지정하면 OR로 동작한다.", example = "true")
    Boolean soldOutOnly,

    @Schema(description = "숨김 항목만 보기. soldOutOnly와 함께 지정하면 OR로 동작한다.", example = "false")
    Boolean hiddenOnly
) {
}

package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "라이더 가게방문 안내 문구 사전 검수 요청")
public record ShopRiderVisitGuideValidateRequest(
    @NotNull(message = "검수할 라이더 가게방문 안내 문구는 필수입니다.")
    @Schema(description = "검수할 라이더 가게방문 안내 문구",
        example = "18인치 피자의 경우 자동차 라이더만 수행 부탁드립니다.", requiredMode = Schema.RequiredMode.REQUIRED)
    String visitGuide
) {
}

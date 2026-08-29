package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * 라이더 가게방문 안내 문구 사전 검수 요청.
 *
 * <p>길이 제한을 Bean Validation으로 걸지 않는다 — 프론트 {@code maxLength}를 우회한 입력도 400이 아니라
 * 위반 사유 목록으로 같은 자리에서 보여주기 위함이다.
 */
@Schema(description = "라이더 가게방문 안내 문구 사전 검수 요청")
public record ShopRiderVisitGuideValidateRequest(
    @NotNull(message = "검수할 라이더 가게방문 안내 문구는 필수입니다.")
    @Schema(description = "검수할 라이더 가게방문 안내 문구",
        example = "18인치 피자의 경우 자동차 라이더만 수행 부탁드립니다.", requiredMode = Schema.RequiredMode.REQUIRED)
    String visitGuide
) {
}

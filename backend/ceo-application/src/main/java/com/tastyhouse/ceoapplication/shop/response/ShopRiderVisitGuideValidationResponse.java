package com.tastyhouse.ceoapplication.shop.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 라이더 가게방문 안내 문구 사전 검수 응답.
 *
 * <p>위반이 있어도 예외가 아니라 200으로 사유 목록을 반환한다 — 검수 결과 자체가 정상 응답이며,
 * 프론트가 저장 실패 토스트가 아니라 인라인 위반 목록으로 보여주기 위함이다.
 */
@Schema(description = "라이더 가게방문 안내 문구 검수 응답")
public record ShopRiderVisitGuideValidationResponse(
    @Schema(description = "위반 없이 등록 가능한지 여부", example = "false")
    boolean valid,

    @Schema(description = "발견된 위반 사유 목록")
    List<String> violations
) {

    public static ShopRiderVisitGuideValidationResponse from(boolean valid, List<String> violations) {
        return new ShopRiderVisitGuideValidationResponse(valid, violations);
    }
}

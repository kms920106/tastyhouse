package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopVisitGuideValidationResult;

@Schema(description = "라이더 가게방문 안내 문구 검수 응답")
public record ShopRiderVisitGuideValidationResponse(
    @Schema(description = "위반 없이 등록 가능한지 여부", example = "false")
    boolean valid,

    @Schema(description = "발견된 위반 사유 목록")
    List<String> violations
) {
    public static ShopRiderVisitGuideValidationResponse from(ShopVisitGuideValidationResult result) {
        return new ShopRiderVisitGuideValidationResponse(
            result.valid(),
            result.violations()
        );
    }
}

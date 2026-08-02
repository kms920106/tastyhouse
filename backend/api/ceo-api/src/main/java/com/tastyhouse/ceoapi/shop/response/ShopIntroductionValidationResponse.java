package com.tastyhouse.ceoapi.shop.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게소개 금칙어 검수 응답")
public record ShopIntroductionValidationResponse(
    @Schema(description = "금칙어 위반 없이 등록 가능한지 여부", example = "false")
    boolean valid,

    @Schema(description = "발견된 금칙어 목록")
    List<String> violations
) {
    public static ShopIntroductionValidationResponse from(boolean valid, List<String> violations) {
        return new ShopIntroductionValidationResponse(valid, violations);
    }
}

package com.tastyhouse.ceoapi.shop.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "가게소개 금칙어 검수 요청")
public record ShopIntroductionValidateRequest(
    @NotBlank(message = "검수할 메시지는 필수입니다.")
    @Schema(description = "검수할 가게소개 메시지", example = "정성을 다해 만드는 맛있는 분식집입니다.",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String message
) {
}

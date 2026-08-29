package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "점주 공지 금칙어 사전 검증 요청")
public record ShopNoticeValidateRequest(
    @NotBlank(message = "공지 본문은 필수입니다.")
    @Size(max = 2000, message = "공지 본문은 최대 2000자까지 입력할 수 있습니다.")
    @Schema(description = "검증할 공지 본문 (최대 2000자)", example = "이번 주 신메뉴 출시했습니다.",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String content
) {
}

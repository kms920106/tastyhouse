package com.tastyhouse.adminapi.shop.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "테하 초이스 등록/수정 요청")
public record ShopChoiceSaveRequest(
    @NotBlank(message = "제목은 필수입니다.")
    @Schema(description = "제목", example = "이번 주 추천 맛집", requiredMode = Schema.RequiredMode.REQUIRED)
    String title,

    @Schema(description = "내용", example = "상세 설명 내용...")
    String content
) {
}

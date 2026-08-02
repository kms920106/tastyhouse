package com.tastyhouse.adminapi.shop.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "테하 초이스 등록 요청")
public record ShopChoiceCreateRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @NotBlank(message = "제목은 필수입니다.")
    @Schema(description = "제목", example = "이번 주 추천 맛집", requiredMode = Schema.RequiredMode.REQUIRED)
    String title,

    @Schema(description = "내용", example = "상세 설명 내용...")
    String content
) {
}

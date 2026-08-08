package com.tastyhouse.adminapi.shop.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 라이더 안내 문구 수정 요청 조치. 문구는 그대로 두고 이력만 남긴다.
 */
@Schema(description = "라이더 안내 문구 수정 요청 조치")
public record ShopRiderVisitGuideRevisionRequest(
    @NotBlank(message = "수정 요청 사유는 필수입니다.")
    @Size(max = 200, message = "수정 요청 사유는 최대 200자까지 입력할 수 있습니다.")
    @Schema(description = "수정 요청 사유 (최대 200자)", example = "배차를 특정하는 문구입니다. 위치 안내로 수정해 주세요.",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String reason
) {
}

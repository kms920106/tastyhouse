package com.tastyhouse.adminapi.point.adapter.in.web.request;

import com.tastyhouse.application.point.port.in.PointEarnCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "포인트 수동 적립 요청")
public record PointEarnRequest(
    @Min(value = 1, message = "적립 포인트는 1 이상이어야 합니다.")
    @Schema(description = "적립할 포인트 금액", example = "1000", requiredMode = Schema.RequiredMode.REQUIRED)
    int amount,

    @NotBlank(message = "적립 사유는 필수입니다.")
    @Size(max = 200, message = "적립 사유는 200자 이하여야 합니다.")
    @Schema(description = "적립 사유", example = "이벤트 보상 지급", requiredMode = Schema.RequiredMode.REQUIRED)
    String reason
) {

    public PointEarnCommand toCommand(Long memberId) {
        return new PointEarnCommand(memberId, amount(), reason());
    }
}

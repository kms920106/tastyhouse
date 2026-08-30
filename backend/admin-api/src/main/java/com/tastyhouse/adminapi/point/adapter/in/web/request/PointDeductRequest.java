package com.tastyhouse.adminapi.point.adapter.in.web.request;

import com.tastyhouse.adminapplication.point.port.in.PointDeductCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "포인트 수동 차감 요청")
public record PointDeductRequest(
    @Min(value = 1, message = "차감 포인트는 1 이상이어야 합니다.")
    @Schema(description = "차감할 포인트 금액", example = "500", requiredMode = Schema.RequiredMode.REQUIRED)
    int amount,

    @NotBlank(message = "차감 사유는 필수입니다.")
    @Size(max = 200, message = "차감 사유는 200자 이하여야 합니다.")
    @Schema(description = "차감 사유", example = "부정 적립 회수", requiredMode = Schema.RequiredMode.REQUIRED)
    String reason
) {

    public PointDeductCommand toCommand(Long memberId) {
        return new PointDeductCommand(memberId, amount(), reason());
    }
}

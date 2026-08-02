package com.tastyhouse.webapi.payment.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "환불 요청")
public record RefundRequest(
    @NotNull(message = "환불 금액은 필수입니다")
    @Min(value = 1, message = "환불 금액은 1원 이상이어야 합니다")
    @Schema(description = "환불 금액", example = "15000", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer refundAmount,

    @Size(max = 500, message = "환불 사유는 500자 이내로 입력해주세요")
    @Schema(description = "환불 사유", example = "상품 불량")
    String refundReason
) {
}

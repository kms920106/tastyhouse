package com.tastyhouse.adminapi.product.adapter.in.web.request;

import com.tastyhouse.application.product.port.in.StorePriceVerificationRejectCommand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "매장 가격 인증 요청 반려 요청")
public record StorePriceVerificationRejectRequest(
    @NotBlank(message = "반려 사유는 필수입니다.")
    @Size(max = 500, message = "반려 사유는 500자 이하여야 합니다.")
    @Schema(description = "반려 사유", example = "가격표 이미지의 금액이 신고된 매장가와 다릅니다.",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String rejectReason
) {
    public StorePriceVerificationRejectCommand toCommand(Long verificationId) {
        return new StorePriceVerificationRejectCommand(verificationId, rejectReason);
    }
}

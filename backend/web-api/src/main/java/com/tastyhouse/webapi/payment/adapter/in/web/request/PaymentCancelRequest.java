package com.tastyhouse.webapi.payment.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.tastyhouse.webapplication.payment.port.in.PaymentCancelCommand;

@Schema(description = "결제 취소 요청")
public record PaymentCancelRequest(
    @NotBlank(message = "취소 사유는 필수입니다")
    @Size(max = 500, message = "취소 사유는 500자 이내로 입력해주세요")
    @Schema(description = "취소 사유", example = "고객 단순 변심", requiredMode = Schema.RequiredMode.REQUIRED)
    String cancelReason
) {

    /**
     * 인증 주체의 {@code memberId}와 경로 변수 {@code paymentId}를 주입받아 command로 변환한다.
     */
    public PaymentCancelCommand toCommand(Long memberId, Long paymentId) {
        return new PaymentCancelCommand(
            memberId,
            paymentId,
            cancelReason
        );
    }
}

package com.tastyhouse.webapi.payment.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.tastyhouse.webapi.payment.application.port.in.PaymentRefundRequestCommand;

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

    /**
     * 인증 주체의 {@code memberId}와 경로 변수 {@code paymentId}를 주입받아 command로 변환한다.
     *
     * <p>두 {@code Long}이 연달아 있어 위치 기반 전달은 조용히 뒤바뀌므로, 아래는 이름 기반 접근자로
     * 각 값을 짚어 넘긴다.
     */
    public PaymentRefundRequestCommand toCommand(Long memberId, Long paymentId) {
        return new PaymentRefundRequestCommand(
            memberId,
            paymentId,
            refundAmount,
            refundReason
        );
    }
}

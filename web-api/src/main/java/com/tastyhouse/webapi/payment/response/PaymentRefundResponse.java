package com.tastyhouse.webapi.payment.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.core.domain.payment.application.dto.result.PaymentRefundResult;

@Schema(description = "결제 환불 응답")
public record PaymentRefundResponse(
    @Schema(description = "환불 ID", example = "1")
    Long id,

    @Schema(description = "결제 ID", example = "1")
    Long paymentId,

    @Schema(description = "환불 금액", example = "21000")
    Integer refundAmount,

    @Schema(description = "환불 사유", example = "고객 변심")
    String refundReason,

    @Schema(description = "환불 상태", example = "COMPLETED")
    String refundStatus,

    @Schema(description = "PG 환불 ID", example = "refund_20260101000001")
    String pgRefundId,

    @Schema(description = "환불 처리 일시", example = "2026-01-01T00:00:00")
    LocalDateTime refundedAt,

    @Schema(description = "환불 생성 일시", example = "2026-01-01T00:00:00")
    LocalDateTime createdAt
) {
    public static PaymentRefundResponse from(PaymentRefundResult result) {
        return new PaymentRefundResponse(
            result.id(),
            result.paymentId(),
            result.refundAmount(),
            result.refundReason(),
            result.refundStatus().name(),
            result.pgRefundId(),
            result.refundedAt(),
            result.createdAt()
        );
    }
}

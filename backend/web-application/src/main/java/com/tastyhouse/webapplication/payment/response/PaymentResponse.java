package com.tastyhouse.webapplication.payment.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "결제 상세 응답")
public record PaymentResponse(
    @Schema(description = "결제 ID", example = "1")
    Long id,

    @Schema(description = "주문 ID", example = "1")
    Long orderId,

    @Schema(description = "결제 수단", example = "CARD")
    String paymentMethod,

    @Schema(description = "결제 상태", example = "APPROVED")
    String paymentStatus,

    @Schema(description = "결제 금액", example = "21000")
    Integer amount,

    @Schema(description = "PG사", example = "TOSS")
    String pgProvider,

    @Schema(description = "PG 거래 ID", example = "tid_2026010100000001")
    String pgTid,

    @Schema(description = "PG 주문 ID", example = "order_20260101000001")
    String pgOrderId,

    @Schema(description = "카드사명", example = "신한카드")
    String cardCompany,

    @Schema(description = "카드 번호(마스킹)", example = "1234-56**-****-7890")
    String cardNumber,

    @Schema(description = "할부 개월 수", example = "0")
    Integer installmentMonths,

    @Schema(description = "결제 승인 일시", example = "2026-01-01T00:00:00")
    LocalDateTime approvedAt,

    @Schema(description = "결제 취소 일시", example = "2026-01-02T00:00:00")
    LocalDateTime cancelledAt,

    @Schema(description = "결제 취소 사유", example = "고객 변심")
    String cancelReason,

    @Schema(description = "결제 영수증 URL", example = "https://cdn.tastyhouse.com/receipt/1.jpg")
    String receiptUrl,

    @Schema(description = "결제 생성 일시", example = "2026-01-01T00:00:00")
    LocalDateTime createdAt
) {
    public static PaymentResponse from(
        Long id,
        Long orderId,
        String paymentMethod,
        String paymentStatus,
        Integer amount,
        String pgProvider,
        String pgTid,
        String pgOrderId,
        String cardCompany,
        String cardNumber,
        Integer installmentMonths,
        LocalDateTime approvedAt,
        LocalDateTime cancelledAt,
        String cancelReason,
        String receiptUrl,
        LocalDateTime createdAt
    ) {
        return new PaymentResponse(
            id,
            orderId,
            paymentMethod,
            paymentStatus,
            amount,
            pgProvider,
            pgTid,
            pgOrderId,
            cardCompany,
            cardNumber,
            installmentMonths,
            approvedAt,
            cancelledAt,
            cancelReason,
            receiptUrl,
            createdAt
        );
    }
}

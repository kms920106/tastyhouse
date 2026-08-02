package com.tastyhouse.webapi.order.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "결제 요약 정보")
public record PaymentSummaryResponse(
    @Schema(description = "결제 ID", example = "1")
    Long id,

    @Schema(description = "결제 수단", example = "CARD")
    String paymentMethod,

    @Schema(description = "결제 상태", example = "APPROVED")
    String paymentStatus,

    @Schema(description = "결제 금액", example = "21000")
    Integer amount,

    @Schema(description = "카드사명", example = "신한카드")
    String cardCompany,

    @Schema(description = "카드 번호(마스킹)", example = "1234-56**-****-7890")
    String cardNumber,

    @Schema(description = "결제 승인 일시", example = "2026-01-01T00:00:00")
    LocalDateTime approvedAt,

    @Schema(description = "결제 영수증 URL", example = "https://cdn.tastyhouse.com/receipt/1.jpg")
    String receiptUrl
) {
    public static PaymentSummaryResponse from(
        Long id,
        String paymentMethod,
        String paymentStatus,
        Integer amount,
        String cardCompany,
        String cardNumber,
        LocalDateTime approvedAt,
        String receiptUrl
    ) {
        return new PaymentSummaryResponse(
            id,
            paymentMethod,
            paymentStatus,
            amount,
            cardCompany,
            cardNumber,
            approvedAt,
            receiptUrl
        );
    }
}

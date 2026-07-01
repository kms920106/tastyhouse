package com.tastyhouse.webapi.payment.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.core.domain.payment.domain.model.PgProvider;

@Schema(description = "결제 승인 요청 (PG 콜백)")
public record PaymentConfirmRequest(
    @NotNull(message = "결제 ID는 필수입니다")
    @Schema(description = "결제 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long paymentId,

    @NotNull(message = "PG사는 필수입니다")
    @Schema(description = "PG사", example = "TOSS", requiredMode = Schema.RequiredMode.REQUIRED)
    PgProvider pgProvider,

    @NotBlank(message = "PG 거래 ID는 필수입니다")
    @Schema(description = "PG 거래 ID", example = "gTAEzXc0iWfF4kVNric9B", requiredMode = Schema.RequiredMode.REQUIRED)
    String pgTid,

    @Schema(description = "PG 주문 ID", example = "0A_rILxddiTVva8R7VddT")
    String pgOrderId,

    @Schema(description = "카드사", example = "신한카드")
    String cardCompany,

    @Schema(description = "카드번호 (마스킹)", example = "123456******1234")
    String cardNumber,

    @Schema(description = "할부 개월 수", example = "0")
    Integer installmentMonths,

    @Schema(description = "영수증 URL", example = "https://receipt.example.com/abc123")
    String receiptUrl
) {
}

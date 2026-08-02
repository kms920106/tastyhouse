package com.tastyhouse.webapi.payment.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "토스 결제 승인 요청")
public record TossPaymentConfirmApiRequest(
    @Schema(description = "토스 결제 키", example = "gTAEzXc0iWfF4kVNric9B")
    @NotBlank(message = "결제 키는 필수입니다")
    String paymentKey,

    @Schema(description = "주문 ID (PG 주문 ID)", example = "0A_rILxddiTVva8R7VddT")
    @NotBlank(message = "주문 ID는 필수입니다")
    String pgOrderId,

    @Schema(description = "결제 금액", example = "15000")
    @NotNull(message = "결제 금액은 필수입니다")
    @Positive(message = "결제 금액은 0보다 커야 합니다")
    Integer amount
) {
}

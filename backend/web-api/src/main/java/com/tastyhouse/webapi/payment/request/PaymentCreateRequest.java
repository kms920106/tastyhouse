package com.tastyhouse.webapi.payment.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "결제 생성 요청")
public record PaymentCreateRequest(
    @NotNull(message = "주문 ID는 필수입니다")
    @Schema(description = "주문 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long orderId,

    @NotBlank(message = "결제 방법은 필수입니다")
    @Schema(description = "결제 방법", example = "CREDIT_CARD", allowableValues = {"CASH_ON_SITE", "CARD_ON_SITE", "CREDIT_CARD", "MOBILE", "KAKAO_PAY", "ZERO_PAY"}, requiredMode = Schema.RequiredMode.REQUIRED)
    String paymentMethod
) {
}

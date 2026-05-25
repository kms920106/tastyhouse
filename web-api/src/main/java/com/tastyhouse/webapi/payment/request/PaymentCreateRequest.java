package com.tastyhouse.webapi.payment.request;

import com.tastyhouse.core.domain.payment.domain.model.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "결제 생성 요청")
public record PaymentCreateRequest(
    @NotNull(message = "주문 ID는 필수입니다")
    @Schema(description = "주문 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long orderId,

    @NotNull(message = "결제 방법은 필수입니다")
    @Schema(description = "결제 방법 (CARD / TOSS / ON_SITE 등)", example = "TOSS", requiredMode = Schema.RequiredMode.REQUIRED)
    PaymentMethod paymentMethod
) {
}

package com.tastyhouse.webapi.payment.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.tastyhouse.webapi.payment.application.port.in.TossPaymentConfirmCommand;

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

    /**
     * 인증 주체의 {@code memberId}를 주입받아 command로 변환한다.
     *
     * <p>{@code paymentKey}·{@code pgOrderId}가 같은 {@code String}이라 위치 기반 전달은 조용히
     * 뒤바뀌므로, 아래는 이름 기반 접근자로 각 값을 짚어 넘긴다.
     */
    public TossPaymentConfirmCommand toCommand(Long memberId) {
        return new TossPaymentConfirmCommand(
            memberId,
            paymentKey,
            pgOrderId,
            amount
        );
    }
}

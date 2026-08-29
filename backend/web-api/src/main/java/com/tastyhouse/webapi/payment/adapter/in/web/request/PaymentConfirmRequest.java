package com.tastyhouse.webapi.payment.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.webapi.payment.application.port.in.PaymentConfirmCommand;

@Schema(description = "결제 승인 요청 (PG 콜백)")
public record PaymentConfirmRequest(
    @NotNull(message = "결제 ID는 필수입니다")
    @Schema(description = "결제 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long paymentId,

    @NotBlank(message = "PG사는 필수입니다")
    @Schema(description = "PG사", example = "TOSS", allowableValues = {"TOSS", "KAKAO", "NICE", "KG_INICIS", "NHN_KCP", "SETTLE_BANK"}, requiredMode = Schema.RequiredMode.REQUIRED)
    String pgProvider,

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

    /**
     * command로 변환한다.
     *
     * <p><b>{@code paymentId}는 본문 필드다</b> — 이 엔드포인트는 경로에 식별자를 두지 않으므로 주입
     * 파라미터가 없다. {@code cardCompany}·{@code cardNumber}·{@code receiptUrl}이 같은 {@code String}
     * 이라 위치 기반 전달은 조용히 뒤바뀌므로, 아래는 이름 기반 접근자로 각 값을 짚어 넘긴다.
     */
    public PaymentConfirmCommand toCommand() {
        return new PaymentConfirmCommand(
            paymentId,
            pgProvider,
            pgTid,
            pgOrderId,
            cardCompany,
            cardNumber,
            installmentMonths,
            receiptUrl
        );
    }
}

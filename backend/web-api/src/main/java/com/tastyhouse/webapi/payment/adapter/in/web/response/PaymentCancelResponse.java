package com.tastyhouse.webapi.payment.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.webapplication.payment.port.out.PaymentCancelResult;

@Schema(description = "결제 취소 응답")
public record PaymentCancelResponse(
    @Schema(description = "결제 취소 결과 코드", example = "SUCCESS")
    String code,

    @Schema(description = "결제 취소 결과 메시지", example = "결제가 정상적으로 취소되었습니다")
    String message
) {
    public static PaymentCancelResponse from(PaymentCancelResult result) {
        return new PaymentCancelResponse(
            result.cancelCode(),
            result.message()
        );
    }
}

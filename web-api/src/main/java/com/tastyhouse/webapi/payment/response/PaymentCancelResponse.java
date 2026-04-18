package com.tastyhouse.webapi.payment.response;

public record PaymentCancelResponse(
    String code,
    String message
) {
    public static PaymentCancelResponse of(PaymentCancelCode cancelCode) {
    return new PaymentCancelResponse(cancelCode.name(), cancelCode.getMessage());
    }
}

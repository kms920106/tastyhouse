package com.tastyhouse.domain.payment.model;

public enum PaymentCancelCode {

    SUCCESS("결제가 취소되었습니다"),
    ALREADY_PREPARING("이미 조리가 시작되어 취소할 수 없습니다"),
    ALREADY_CANCELLED("이미 취소된 주문입니다"),
    ORDER_COMPLETED("이미 완료된 주문은 취소할 수 없습니다"),
    CANCEL_FAILED("결제 취소에 실패했습니다");

    private final String message;

    PaymentCancelCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}

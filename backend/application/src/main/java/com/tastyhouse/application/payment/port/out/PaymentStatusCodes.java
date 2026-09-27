package com.tastyhouse.application.payment.port.out;

public final class PaymentStatusCodes {
    public static final String PENDING = "PENDING";
    public static final String COMPLETED = "COMPLETED";
    public static final String FAILED = "FAILED";
    public static final String CANCELLED = "CANCELLED";

    private PaymentStatusCodes() {
    }
}

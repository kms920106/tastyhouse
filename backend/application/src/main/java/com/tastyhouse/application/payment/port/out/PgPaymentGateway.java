package com.tastyhouse.application.payment.port.out;

public interface PgPaymentGateway {
    boolean supports(String pgProvider);

    PgConfirmResult confirmPayment(String pgProvider, Long paymentId, String paymentKey, String pgOrderId, int amount);

    PgCancelResult cancelPayment(String pgProvider, String pgTid, String cancelReason);
}

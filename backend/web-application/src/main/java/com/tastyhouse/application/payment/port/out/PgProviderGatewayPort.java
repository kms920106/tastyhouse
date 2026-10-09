package com.tastyhouse.application.payment.port.out;

public interface PgProviderGatewayPort {

    PgProviderCode provider();

    PgConfirmResult confirmPayment(Long paymentId, String paymentKey, String pgOrderId, int amount);

    PgCancelResult cancelPayment(String pgTid, String cancelReason);
}

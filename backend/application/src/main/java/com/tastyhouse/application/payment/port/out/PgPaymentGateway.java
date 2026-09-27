package com.tastyhouse.application.payment.port.out;

import com.tastyhouse.domain.payment.model.PgProvider;

public interface PgPaymentGateway {
    boolean supports(PgProvider pgProvider);

    PgConfirmResult confirmPayment(PgProvider pgProvider, Long paymentId, String paymentKey, String pgOrderId, int amount);

    PgCancelResult cancelPayment(PgProvider pgProvider, String pgTid, String cancelReason);
}

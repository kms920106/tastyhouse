package com.tastyhouse.domain.payment.port;

import com.tastyhouse.domain.payment.model.PgProvider;
import com.tastyhouse.domain.payment.port.dto.PgCancelResult;
import com.tastyhouse.domain.payment.port.dto.PgConfirmResult;

public interface PgPaymentGateway {
    boolean supports(PgProvider pgProvider);

    PgConfirmResult confirmPayment(PgProvider pgProvider, Long paymentId, String paymentKey, String pgOrderId, int amount);

    PgCancelResult cancelPayment(PgProvider pgProvider, String pgTid, String cancelReason);
}

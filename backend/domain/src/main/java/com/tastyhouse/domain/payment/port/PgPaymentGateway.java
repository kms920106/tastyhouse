package com.tastyhouse.domain.payment.port;

import com.tastyhouse.domain.payment.port.dto.PgCancelResult;
import com.tastyhouse.domain.payment.port.dto.PgConfirmResult;

public interface PgPaymentGateway {
    PgConfirmResult confirmPayment(Long paymentId, String paymentKey, String pgOrderId, int amount);

    PgCancelResult cancelPayment(String pgTid, String cancelReason);
}

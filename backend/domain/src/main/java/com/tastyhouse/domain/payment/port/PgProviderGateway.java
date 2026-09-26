package com.tastyhouse.domain.payment.port;

import com.tastyhouse.domain.payment.model.PgProvider;
import com.tastyhouse.domain.payment.port.dto.PgCancelResult;
import com.tastyhouse.domain.payment.port.dto.PgConfirmResult;

public interface PgProviderGateway {
    PgProvider provider();

    PgConfirmResult confirmPayment(Long paymentId, String paymentKey, String pgOrderId, int amount);

    PgCancelResult cancelPayment(String pgTid, String cancelReason);
}

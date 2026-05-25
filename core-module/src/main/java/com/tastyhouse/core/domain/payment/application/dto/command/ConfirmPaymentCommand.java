package com.tastyhouse.core.domain.payment.application.dto.command;

import com.tastyhouse.core.domain.payment.domain.model.PgProvider;

public record ConfirmPaymentCommand(
    Long paymentId,
    PgProvider pgProvider,
    String pgTid,
    String pgOrderId,
    String cardCompany,
    String cardNumber,
    Integer installmentMonths,
    String receiptUrl
) {
}

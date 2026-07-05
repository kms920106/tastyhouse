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

    public static ConfirmPaymentCommand of(
        Long paymentId,
        PgProvider pgProvider,
        String pgTid,
        String pgOrderId,
        String cardCompany,
        String cardNumber,
        Integer installmentMonths,
        String receiptUrl
    ) {
        return new ConfirmPaymentCommand(
            paymentId, pgProvider, pgTid, pgOrderId, cardCompany, cardNumber, installmentMonths, receiptUrl
        );
    }
}

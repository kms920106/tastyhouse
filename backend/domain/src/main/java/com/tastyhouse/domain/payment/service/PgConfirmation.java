package com.tastyhouse.domain.payment.service;

import com.tastyhouse.domain.payment.model.PgProvider;

public record PgConfirmation(
    PgProvider pgProvider,
    String pgTid,
    String pgOrderId,
    String cardCompany,
    String cardNumber,
    Integer installmentMonths,
    String receiptUrl
) {
    public static PgConfirmation of(
        PgProvider pgProvider,
        String pgTid,
        String pgOrderId,
        String cardCompany,
        String cardNumber,
        Integer installmentMonths,
        String receiptUrl
    ) {
        return new PgConfirmation(
            pgProvider,
            pgTid,
            pgOrderId,
            cardCompany,
            cardNumber,
            installmentMonths,
            receiptUrl
        );
    }
}

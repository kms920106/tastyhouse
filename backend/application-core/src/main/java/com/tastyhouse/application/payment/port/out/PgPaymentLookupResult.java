package com.tastyhouse.application.payment.port.out;

public record PgPaymentLookupResult(
    PgPaymentLookupStatus status,
    PgConfirmResult result
) {
}

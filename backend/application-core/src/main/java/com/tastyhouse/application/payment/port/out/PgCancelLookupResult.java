package com.tastyhouse.application.payment.port.out;

public record PgCancelLookupResult(
    PgCancelLookupStatus status,
    String pgStatus
) {
}

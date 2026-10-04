package com.tastyhouse.application.payment.port.out;

public record PgCancelResult(
    boolean success,
    String errorCode,
    String errorMessage
) {
}

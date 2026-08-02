package com.tastyhouse.domain.payment.port.dto;

public record PgCancelResult(
    boolean success,
    String errorCode,
    String errorMessage
) {
}

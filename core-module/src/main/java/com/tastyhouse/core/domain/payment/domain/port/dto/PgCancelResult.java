package com.tastyhouse.core.domain.payment.domain.port.dto;

public record PgCancelResult(
    boolean success,
    String errorCode,
    String errorMessage
) {
}

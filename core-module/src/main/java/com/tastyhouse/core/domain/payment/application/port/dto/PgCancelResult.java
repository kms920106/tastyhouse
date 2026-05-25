package com.tastyhouse.core.domain.payment.application.port.dto;

public record PgCancelResult(
    boolean success,
    String errorCode,
    String errorMessage
) {
}

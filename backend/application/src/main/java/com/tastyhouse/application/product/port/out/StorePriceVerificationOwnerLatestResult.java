package com.tastyhouse.application.product.port.out;

public record StorePriceVerificationOwnerLatestResult(
    Long id,
    String status,
    String rejectReason
) {
}

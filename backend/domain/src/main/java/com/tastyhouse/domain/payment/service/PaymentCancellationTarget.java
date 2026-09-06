package com.tastyhouse.domain.payment.service;

import com.tastyhouse.domain.payment.model.PaymentCancelCode;

public record PaymentCancellationTarget(
    PaymentCancelCode rejectCode,
    boolean pgCancelRequired,
    String pgTid
) {
    public static PaymentCancellationTarget rejected(PaymentCancelCode rejectCode) {
        return new PaymentCancellationTarget(
            rejectCode,
            false,
            null
        );
    }

    public static PaymentCancellationTarget cancellable(boolean pgCancelRequired, String pgTid) {
        return new PaymentCancellationTarget(
            null,
            pgCancelRequired,
            pgTid
        );
    }

    public boolean isRejected() {
        return rejectCode != null;
    }
}

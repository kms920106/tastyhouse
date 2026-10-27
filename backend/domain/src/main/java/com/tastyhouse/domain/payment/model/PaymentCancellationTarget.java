package com.tastyhouse.domain.payment.model;

public record PaymentCancellationTarget(
    PaymentCancelCode rejectCode,
    boolean pgCancelRequired,
    PgProvider pgProvider,
    String pgTid,
    String pgOrderId
) {

    public static PaymentCancellationTarget rejected(PaymentCancelCode rejectCode) {
        return new PaymentCancellationTarget(
            rejectCode,
            false,
            null,
            null,
            null
        );
    }

    public static PaymentCancellationTarget cancellable(
        boolean pgCancelRequired,
        PgProvider pgProvider,
        String pgTid,
        String pgOrderId
    ) {
        return new PaymentCancellationTarget(
            null,
            pgCancelRequired,
            pgProvider,
            pgTid,
            pgOrderId
        );
    }

    public boolean isRejected() {
        return rejectCode != null;
    }
}

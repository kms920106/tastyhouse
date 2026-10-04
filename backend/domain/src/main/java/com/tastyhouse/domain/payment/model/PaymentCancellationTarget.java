package com.tastyhouse.domain.payment.model;

public record PaymentCancellationTarget(
    PaymentCancelCode rejectCode,
    boolean pgCancelRequired,
    PgProvider pgProvider,
    String pgTid
) {

    public static PaymentCancellationTarget rejected(PaymentCancelCode rejectCode) {
        return new PaymentCancellationTarget(
            rejectCode,
            false,
            null,
            null
        );
    }

    public static PaymentCancellationTarget cancellable(
        boolean pgCancelRequired,
        PgProvider pgProvider,
        String pgTid
    ) {
        return new PaymentCancellationTarget(
            null,
            pgCancelRequired,
            pgProvider,
            pgTid
        );
    }

    public boolean isRejected() {
        return rejectCode != null;
    }
}

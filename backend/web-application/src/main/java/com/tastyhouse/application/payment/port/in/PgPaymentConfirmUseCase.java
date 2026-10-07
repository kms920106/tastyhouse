package com.tastyhouse.application.payment.port.in;

public interface PgPaymentConfirmUseCase {

    Long confirmPgPayment(PgPaymentConfirmCommand command);
}

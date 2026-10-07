package com.tastyhouse.application.payment.port.in;

public interface PaymentConfirmUseCase {

    Long confirmPayment(PaymentConfirmCommand command);
}

package com.tastyhouse.application.payment.port.in;

public interface PaymentCreateUseCase {

    Long createPayment(PaymentCreateCommand command);
}

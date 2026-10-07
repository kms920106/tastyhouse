package com.tastyhouse.application.payment.port.in;

public interface PaymentRefundRequestUseCase {

    Long requestRefund(PaymentRefundRequestCommand command);
}

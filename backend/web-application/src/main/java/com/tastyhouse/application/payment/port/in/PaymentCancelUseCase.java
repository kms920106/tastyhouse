package com.tastyhouse.application.payment.port.in;

import com.tastyhouse.application.payment.port.out.PaymentCancelResult;

public interface PaymentCancelUseCase {

    PaymentCancelResult cancelPayment(PaymentCancelCommand command);
}

package com.tastyhouse.application.payment.port.in;

import com.tastyhouse.application.payment.port.out.PaymentViewResult;

public interface PaymentDetailByIdQueryUseCase {

    PaymentViewResult getPayment(Long id);
}

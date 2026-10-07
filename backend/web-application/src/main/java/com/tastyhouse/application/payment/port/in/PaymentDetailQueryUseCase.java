package com.tastyhouse.application.payment.port.in;

import com.tastyhouse.application.payment.port.out.PaymentViewResult;

public interface PaymentDetailQueryUseCase {

    PaymentViewResult getPayment(Long memberId, Long id);
}

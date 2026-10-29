package com.tastyhouse.application.payment.port.in;

public interface PaymentOnSiteCompleteUseCase {

    Long completeOnSitePayment(PaymentOnSiteCompleteCommand command);
}

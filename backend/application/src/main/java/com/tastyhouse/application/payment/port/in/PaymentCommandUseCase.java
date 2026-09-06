package com.tastyhouse.application.payment.port.in;

import com.tastyhouse.application.shared.marker.WebApp;
import com.tastyhouse.application.payment.port.out.PaymentCancelResult;

@WebApp
public interface PaymentCommandUseCase {

    Long createPayment(PaymentCreateCommand command);

    Long confirmPayment(PaymentConfirmCommand command);

    Long confirmTossPayment(TossPaymentConfirmCommand command);

    Long completeOnSitePayment(PaymentOnSiteCompleteCommand command);

    PaymentCancelResult cancelPayment(PaymentCancelCommand command);

    Long requestRefund(PaymentRefundRequestCommand command);
}

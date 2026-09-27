package com.tastyhouse.application.payment.port.in;

import com.tastyhouse.application.payment.port.out.PaymentCancelResult;
import com.tastyhouse.application.shared.marker.WebApp;

@WebApp
public interface PaymentCommandUseCase {

    Long createPayment(PaymentCreateCommand command);

    Long confirmPayment(PaymentConfirmCommand command);

    Long confirmPgPayment(PgPaymentConfirmCommand command);

    Long completeOnSitePayment(PaymentOnSiteCompleteCommand command);

    PaymentCancelResult cancelPayment(PaymentCancelCommand command);

    Long requestRefund(PaymentRefundRequestCommand command);
}

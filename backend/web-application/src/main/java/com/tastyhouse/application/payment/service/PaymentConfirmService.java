package com.tastyhouse.application.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.payment.model.PgProvider;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.application.payment.port.in.PaymentConfirmCommand;
import com.tastyhouse.application.payment.port.in.PaymentConfirmUseCase;

@Service
@Transactional
class PaymentConfirmService implements PaymentConfirmUseCase {

    private final PaymentConfirmationService paymentConfirmationService;

    public PaymentConfirmService(PaymentConfirmationService paymentConfirmationService) {
        this.paymentConfirmationService = paymentConfirmationService;
    }

    @Override
    public Long confirmPayment(PaymentConfirmCommand command) {
        PaymentId paymentId = PaymentId.of(command.paymentId());
        PgConfirmation confirmation = PgConfirmation.of(
            PgProvider.from(command.pgProvider()),
            command.pgTid(),
            command.pgOrderId(),
            command.cardCompany(),
            command.cardNumber(),
            command.installmentMonths(),
            command.receiptUrl()
        );
        return paymentConfirmationService.confirm(paymentId, confirmation).value();
    }
}

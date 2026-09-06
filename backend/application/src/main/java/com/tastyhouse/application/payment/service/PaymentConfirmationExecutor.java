package com.tastyhouse.application.payment.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.payment.port.dto.PgConfirmResult;
import com.tastyhouse.domain.payment.service.PaymentConfirmationService;
import com.tastyhouse.domain.payment.service.TossConfirmationTarget;
import com.tastyhouse.domain.payment.vo.PaymentId;

@Component
@WebApp
public class PaymentConfirmationExecutor {

    private final PaymentConfirmationService paymentConfirmationService;

    public PaymentConfirmationExecutor(PaymentConfirmationService paymentConfirmationService) {
        this.paymentConfirmationService = paymentConfirmationService;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public TossConfirmationTarget prepareInNewTx(MemberId memberId, String pgOrderId, int amount) {
        return paymentConfirmationService.prepareTossConfirmation(memberId, pgOrderId, amount);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public PaymentId applyInNewTx(MemberId memberId, String pgOrderId, PgConfirmResult result) {
        return paymentConfirmationService.applyTossConfirmation(memberId, pgOrderId, result);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void failInNewTx(String pgOrderId, PgConfirmResult result) {
        paymentConfirmationService.failTossConfirmation(pgOrderId, result);
    }
}

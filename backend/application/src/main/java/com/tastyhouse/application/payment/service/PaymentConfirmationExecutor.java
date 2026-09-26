package com.tastyhouse.application.payment.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.payment.model.PgProvider;
import com.tastyhouse.domain.payment.port.dto.PgConfirmResult;
import com.tastyhouse.domain.payment.service.PaymentConfirmationService;
import com.tastyhouse.domain.payment.service.PgConfirmationTarget;
import com.tastyhouse.domain.payment.vo.PaymentId;

@Component
@WebApp
public class PaymentConfirmationExecutor {

    private final PaymentConfirmationService paymentConfirmationService;

    public PaymentConfirmationExecutor(PaymentConfirmationService paymentConfirmationService) {
        this.paymentConfirmationService = paymentConfirmationService;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public PgConfirmationTarget prepareInNewTx(MemberId memberId, String pgOrderId, int amount) {
        return paymentConfirmationService.preparePgConfirmation(memberId, pgOrderId, amount);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public PaymentId applyInNewTx(MemberId memberId, PgProvider pgProvider, String pgOrderId, PgConfirmResult result) {
        return paymentConfirmationService.applyPgConfirmation(memberId, pgProvider, pgOrderId, result);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void failInNewTx(String pgOrderId, PgConfirmResult result) {
        paymentConfirmationService.failPgConfirmation(pgOrderId, result);
    }
}

package com.tastyhouse.application.payment.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.payment.model.PaymentCancelCode;
import com.tastyhouse.domain.payment.service.PaymentCancellationService;
import com.tastyhouse.domain.payment.service.PaymentCancellationTarget;
import com.tastyhouse.domain.payment.vo.PaymentId;

@Component
@WebApp
public class PaymentCancellationExecutor {

    private final PaymentCancellationService paymentCancellationService;

    public PaymentCancellationExecutor(PaymentCancellationService paymentCancellationService) {
        this.paymentCancellationService = paymentCancellationService;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public PaymentCancellationTarget prepareInNewTx(MemberId memberId, PaymentId paymentId) {
        return paymentCancellationService.prepareCancellation(memberId, paymentId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public PaymentCancelCode applyInNewTx(MemberId memberId, PaymentId paymentId, String cancelReason) {
        return paymentCancellationService.applyCancellation(memberId, paymentId, cancelReason);
    }
}

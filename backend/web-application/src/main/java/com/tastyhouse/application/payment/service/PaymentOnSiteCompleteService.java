package com.tastyhouse.application.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.application.payment.port.in.PaymentOnSiteCompleteCommand;
import com.tastyhouse.application.payment.port.in.PaymentOnSiteCompleteUseCase;

@Service
@Transactional
class PaymentOnSiteCompleteService implements PaymentOnSiteCompleteUseCase {

    private final PaymentConfirmationService paymentConfirmationService;

    public PaymentOnSiteCompleteService(PaymentConfirmationService paymentConfirmationService) {
        this.paymentConfirmationService = paymentConfirmationService;
    }

    @Override
    public Long completeOnSitePayment(PaymentOnSiteCompleteCommand command) {
        MemberId memberIdVo = MemberId.of(command.memberId());
        PaymentId paymentId = PaymentId.of(command.paymentId());
        return paymentConfirmationService.completeOnSitePayment(memberIdVo, paymentId).value();
    }
}

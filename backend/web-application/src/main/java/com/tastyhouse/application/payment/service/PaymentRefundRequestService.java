package com.tastyhouse.application.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.application.payment.port.in.PaymentRefundRequestCommand;
import com.tastyhouse.application.payment.port.in.PaymentRefundRequestUseCase;

@Service
@Transactional
class PaymentRefundRequestService implements PaymentRefundRequestUseCase {

    private final PaymentCancellationService paymentCancellationService;

    public PaymentRefundRequestService(PaymentCancellationService paymentCancellationService) {
        this.paymentCancellationService = paymentCancellationService;
    }

    @Override
    public Long requestRefund(PaymentRefundRequestCommand command) {
        MemberId memberIdVo = MemberId.of(command.memberId());
        PaymentId paymentId = PaymentId.of(command.paymentId());
        return paymentCancellationService
            .requestRefund(memberIdVo, paymentId, command.refundAmount(), command.refundReason())
            .value();
    }
}

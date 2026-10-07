package com.tastyhouse.application.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.payment.model.PaymentMethod;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.application.payment.port.in.PaymentCreateCommand;
import com.tastyhouse.application.payment.port.in.PaymentCreateUseCase;

@Service
@Transactional
class PaymentCreateService implements PaymentCreateUseCase {

    private final PaymentConfirmationService paymentConfirmationService;

    public PaymentCreateService(PaymentConfirmationService paymentConfirmationService) {
        this.paymentConfirmationService = paymentConfirmationService;
    }

    @Override
    public Long createPayment(PaymentCreateCommand command) {
        MemberId memberIdVo = MemberId.of(command.memberId());
        OrderId orderIdVo = OrderId.of(command.orderId());
        PaymentId paymentId = paymentConfirmationService.open(
            memberIdVo, orderIdVo, PaymentMethod.from(command.paymentMethod())
        );
        return paymentId.value();
    }
}

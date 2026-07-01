package com.tastyhouse.core.domain.payment.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.order.application.OrderQueryService;
import com.tastyhouse.core.domain.order.domain.model.Order;
import com.tastyhouse.core.domain.order.domain.vo.OrderId;
import com.tastyhouse.core.domain.payment.application.dto.result.PaymentResult;
import com.tastyhouse.core.domain.payment.domain.model.Payment;
import com.tastyhouse.core.domain.payment.domain.repository.PaymentRepository;
import com.tastyhouse.core.exception.AccessDeniedException;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class PaymentQueryService {

    private final PaymentRepository paymentRepository;
    private final OrderQueryService orderQueryService;

    @Transactional(readOnly = true)
    public PaymentResult getPaymentByOrderId(Long memberId, Long orderIdValue) {
        Order order = orderQueryService.findById(orderIdValue)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getMemberId().equals(memberId)) {
            throw new AccessDeniedException(ErrorCode.ORDER_ACCESS_DENIED);
        }

        Payment payment = paymentRepository.findByOrderId(new OrderId(orderIdValue))
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "결제 정보를 찾을 수 없습니다."));

        return PaymentResult.from(payment);
    }
}

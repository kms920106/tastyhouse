package com.tastyhouse.webapi.payment;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.payment.domain.model.PaymentCancelCode;
import com.tastyhouse.domain.payment.domain.service.PaymentCancellationService;
import com.tastyhouse.domain.payment.domain.service.PaymentCancellationTarget;
import com.tastyhouse.domain.payment.domain.vo.PaymentId;

/**
 * 결제 취소의 트랜잭션 경계 빈.
 *
 * <p>PG 취소 요청을 DB 트랜잭션 밖으로 빼기 위해 취소가 "사전 판정 → PG 취소 요청 → 결과 반영" 3단으로
 * 쪼개졌고, 이 빈은 그 중 <b>DB를 만지는 두 단계 각각을 독립 트랜잭션으로 감싸는 얇은 위임</b>만 담당한다.
 * 비즈니스 로직은 갖지 않는다. 별도 빈이 필요한 이유는 {@link PaymentConfirmationExecutor}와 같다
 * (self-invocation으로는 {@code @Transactional}이 적용되지 않고, 도메인 서비스는 POJO라 가질 수 없다).
 */
@Component
public class PaymentCancellationExecutor {

    private final PaymentCancellationService paymentCancellationService;

    public PaymentCancellationExecutor(PaymentCancellationService paymentCancellationService) {
        this.paymentCancellationService = paymentCancellationService;
    }

    /**
     * 1단(트랜잭션) — PG 취소 요청 전 판정. 읽기만 하므로 {@code readOnly}다.
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public PaymentCancellationTarget prepareInNewTx(MemberId memberId, PaymentId paymentId) {
        return paymentCancellationService.prepareCancellation(memberId, paymentId);
    }

    /**
     * 2단(트랜잭션) — 결제·주문 취소 전이 반영.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public PaymentCancelCode applyInNewTx(MemberId memberId, PaymentId paymentId, String cancelReason) {
        return paymentCancellationService.applyCancellation(memberId, paymentId, cancelReason);
    }
}

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

/**
 * 토스 결제 승인의 트랜잭션 경계 빈.
 *
 * <p>PG HTTP 왕복을 DB 트랜잭션 밖으로 빼기 위해 승인이 "사전 검증 → PG 호출 → 결과 반영" 3단으로 쪼개졌고,
 * 이 빈은 그 중 <b>DB를 만지는 두 단계 각각을 독립 트랜잭션으로 감싸는 얇은 위임</b>만 담당한다
 * (reservation 도메인의 {@code ReservationBookingExecutor}와 같은 역할). 비즈니스 로직은 갖지 않는다.
 *
 * <p><b>왜 별도 빈인가</b>: 오케스트레이션을 하는 {@link PaymentCommandService}는 PG 호출을 트랜잭션 밖에
 * 두어야 하므로 {@code @Transactional}을 가질 수 없다. 그런데 같은 빈의 메서드를 호출하면 Spring 프록시를
 * 거치지 않아(self-invocation) {@code @Transactional}이 적용되지 않고, 도메인 서비스는 순수 POJO라
 * {@code @Transactional}을 가질 수 없다. 그래서 트랜잭션 경계를 담당하는 얇은 빈이 별도로 필요하다.
 *
 * <p>{@code REQUIRES_NEW}가 아니라 기본 {@code REQUIRED}를 쓴다 — 호출자({@code PaymentCommandService})가
 * 트랜잭션을 열지 않으므로 각 호출이 자연히 새 트랜잭션이 되며, 상위 트랜잭션이 없는 상태를 전제로 한
 * 설계임을 명시하려 전파 속성을 그대로 남긴다.
 */
@Component
@WebApp
public class PaymentConfirmationExecutor {

    private final PaymentConfirmationService paymentConfirmationService;

    public PaymentConfirmationExecutor(PaymentConfirmationService paymentConfirmationService) {
        this.paymentConfirmationService = paymentConfirmationService;
    }

    /**
     * 1단(트랜잭션) — PG 호출 전 검증. 읽기만 하므로 {@code readOnly}다.
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public TossConfirmationTarget prepareInNewTx(MemberId memberId, String pgOrderId, int amount) {
        return paymentConfirmationService.prepareTossConfirmation(memberId, pgOrderId, amount);
    }

    /**
     * 2단(트랜잭션, 성공) — PG 승인 결과 반영.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public PaymentId applyInNewTx(MemberId memberId, String pgOrderId, PgConfirmResult result) {
        return paymentConfirmationService.applyTossConfirmation(memberId, pgOrderId, result);
    }

    /**
     * 2단(트랜잭션, 실패) — PG 승인 거절 결과 반영(원장 기록 + {@code FAILED} 전이).
     *
     * <p>이 트랜잭션은 <b>커밋되어야 한다</b> — 실패 사실과 PG 응답 원본을 남기는 것이 목적이므로,
     * 예외 변환({@code PAYMENT_APPROVAL_FAILED})은 커밋 이후 호출자가 수행한다.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void failInNewTx(String pgOrderId, PgConfirmResult result) {
        paymentConfirmationService.failTossConfirmation(pgOrderId, result);
    }
}

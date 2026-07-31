package com.tastyhouse.domain.payment.domain.service;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.order.domain.model.Order;
import com.tastyhouse.domain.order.domain.model.OrderStatus;
import com.tastyhouse.domain.order.domain.service.OrderTransitionService;
import com.tastyhouse.domain.payment.domain.event.PaymentCancelledEvent;
import com.tastyhouse.domain.payment.domain.event.RefundRequestedEvent;
import com.tastyhouse.domain.payment.domain.model.Payment;
import com.tastyhouse.domain.payment.domain.model.PaymentCancelCode;
import com.tastyhouse.domain.payment.domain.model.PaymentRefund;
import com.tastyhouse.domain.payment.domain.model.PaymentStatus;
import com.tastyhouse.domain.payment.domain.model.PgProvider;
import com.tastyhouse.domain.payment.domain.port.PgPaymentGateway;
import com.tastyhouse.domain.payment.domain.port.dto.PgCancelResult;
import com.tastyhouse.domain.payment.domain.repository.PaymentRefundRepository;
import com.tastyhouse.domain.payment.domain.repository.PaymentRepository;
import com.tastyhouse.domain.payment.domain.vo.Amount;
import com.tastyhouse.domain.payment.domain.vo.PaymentId;
import com.tastyhouse.domain.payment.domain.vo.PaymentRefundId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.EntityNotFoundException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

/**
 * 결제 취소·환불 불변식(도메인 서비스).
 *
 * <p>결제 취소는 결제 애그리거트({@link Payment})의 취소 전이와 주문 애그리거트({@link Order})의 취소
 * 전이를 한 트랜잭션에서 반드시 함께 수행해야 하는 원자 연산이다(분류 C) — 한쪽만 반영되면 "결제는
 * 취소됐지만 주문은 살아 있는" 정합성 붕괴가 남는다. 여기에 PG 취소 요청과 포인트 원복
 * (이벤트 리스너 경유)까지 묶이므로 도메인 계층에 둔다. 환불 요청({@link #requestRefund})은 결제 한
 * 건만 다루지만 취소·환불이 같은 관심사이므로 같은 서비스가 갖는다.
 *
 * <p>주문 상태 전이는 직접 {@code order.cancel()}을 호출하지 않고 40-order의
 * {@link OrderTransitionService}에 위임한다 — 전이와 저장을 항상 함께 수행한다는 규칙의 단일 원천을
 * 주문 도메인에 유지하기 위함이다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code DomainServiceConfig}가 담당한다. 트랜잭션 경계는 이 서비스를 호출하는
 * 소비 모듈의 command 서비스(web-api {@code PaymentCommandService})가 선언한다.
 *
 * <p>이벤트 발행은 Spring {@code ApplicationEventPublisher}가 아니라 프레임워크-프리 포트
 * {@link DomainEventPublisher}를 쓴다.
 *
 * <p>도메인 모델이 순수 POJO라 더티 체킹이 없으므로 전이 후 명시적으로 {@code save}를 호출한다.
 */
public class PaymentCancellationService {

    private final PaymentRepository paymentRepository;
    private final PaymentRefundRepository paymentRefundRepository;
    private final PgPaymentGateway pgPaymentGateway;
    private final OrderTransitionService orderTransitionService;
    private final DomainEventPublisher domainEventPublisher;

    public PaymentCancellationService(
        PaymentRepository paymentRepository,
        PaymentRefundRepository paymentRefundRepository,
        PgPaymentGateway pgPaymentGateway,
        OrderTransitionService orderTransitionService,
        DomainEventPublisher domainEventPublisher
    ) {
        this.paymentRepository = paymentRepository;
        this.paymentRefundRepository = paymentRefundRepository;
        this.pgPaymentGateway = pgPaymentGateway;
        this.orderTransitionService = orderTransitionService;
        this.domainEventPublisher = domainEventPublisher;
    }

    /**
     * 결제를 취소한다 — 주문 상태로 취소 가능 여부를 판정하고, 완료된 토스 결제면 PG 취소를 먼저 요청한
     * 뒤 결제·주문을 함께 취소 전이한다.
     *
     * <p>취소 불가 사유(조리 시작·이미 취소·주문 완료)와 PG 취소 실패는 예외가 아니라
     * {@link PaymentCancelCode}로 돌려준다 — 호출자가 사유를 그대로 사용자에게 안내해야 하고, 이 경우
     * 어떤 상태도 바꾸지 않기 때문이다(기존 동작 보존).
     *
     * @return 취소 결과 코드 — {@link PaymentCancelCode#SUCCESS}면 취소가 반영됨
     */
    public PaymentCancelCode cancel(MemberId memberId, PaymentId paymentId, String cancelReason) {
        Payment payment = loadPayment(paymentId);
        Order order = orderTransitionService.loadOwnedBy(
            payment.getOrderId(), memberId, ErrorCode.PAYMENT_ACCESS_DENIED
        );

        PaymentCancelCode cancelCode = resolveCancelCode(order.getOrderStatus());
        if (cancelCode != PaymentCancelCode.SUCCESS) {
            return cancelCode;
        }

        if (isPgCancelRequired(payment) && !requestPgCancel(payment, cancelReason)) {
            return PaymentCancelCode.CANCEL_FAILED;
        }

        LocalDateTime now = LocalDateTime.now();
        payment.cancel(cancelReason, now);

        paymentRepository.save(payment);
        orderTransitionService.cancel(order);

        domainEventPublisher.publish(new PaymentCancelledEvent(
            paymentId,
            payment.getOrderId(),
            memberId,
            order.getUsedPoint(),
            order.getEarnedPoint(),
            cancelReason,
            now
        ));

        return PaymentCancelCode.SUCCESS;
    }

    /**
     * 환불을 요청한다 — 완료된 결제만, 결제 금액을 넘지 않는 범위에서 접수한다.
     *
     * @return 생성된 환불 요청 식별자
     */
    public PaymentRefundId requestRefund(
        MemberId memberId,
        PaymentId paymentId,
        int refundAmount,
        String refundReason
    ) {
        Payment payment = loadPayment(paymentId);
        orderTransitionService.loadOwnedBy(payment.getOrderId(), memberId, ErrorCode.PAYMENT_ACCESS_DENIED);

        if (payment.getPaymentStatus() != PaymentStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_COMPLETED);
        }

        if (refundAmount > payment.getAmount().value()) {
            throw new BusinessException(ErrorCode.PAYMENT_REFUND_AMOUNT_EXCEEDED);
        }

        Amount amount = new Amount(refundAmount);
        PaymentRefund savedRefund = paymentRefundRepository.save(
            PaymentRefund.create(paymentId, amount, refundReason)
        );

        domainEventPublisher.publish(new RefundRequestedEvent(
            savedRefund.getPaymentRefundId(),
            paymentId,
            memberId,
            amount,
            refundReason,
            LocalDateTime.now()
        ));

        return savedRefund.getPaymentRefundId();
    }

    private Payment loadPayment(PaymentId paymentId) {
        return paymentRepository.findById(paymentId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "결제를 찾을 수 없습니다."));
    }

    /**
     * 주문 상태로 취소 가능 여부를 판정한다 — 조리가 시작되었거나 이미 종결된 주문은 취소할 수 없다.
     */
    private PaymentCancelCode resolveCancelCode(OrderStatus orderStatus) {
        return switch (orderStatus) {
            case PREPARING -> PaymentCancelCode.ALREADY_PREPARING;
            case CANCELLED -> PaymentCancelCode.ALREADY_CANCELLED;
            case COMPLETED -> PaymentCancelCode.ORDER_COMPLETED;
            case PENDING, CONFIRMED -> PaymentCancelCode.SUCCESS;
        };
    }

    /**
     * PG 취소 요청이 필요한 결제인지 — 실제로 승인이 완료된 토스 결제만 대상이다.
     */
    private boolean isPgCancelRequired(Payment payment) {
        return payment.getPgProvider() == PgProvider.TOSS
            && payment.getPaymentStatus() == PaymentStatus.COMPLETED;
    }

    /**
     * PG 취소를 요청한다 — 실패·예외 모두 취소 불가로 간주해 {@code false}를 돌려주고, 어떤 상태도
     * 바꾸지 않는다(기존 동작 보존).
     *
     * <p>실패 사유 로깅은 이 POJO가 아니라 트랜잭션 경계를 가진 소비 모듈의 command 서비스가 담당한다 —
     * 도메인 서비스는 프레임워크(로깅 포함)에 의존하지 않는다(공통 지침 패턴 1). 호출자는 돌려받은
     * {@link PaymentCancelCode#CANCEL_FAILED}로 실패를 인지해 기록한다.
     */
    private boolean requestPgCancel(Payment payment, String cancelReason) {
        try {
            PgCancelResult cancelResult = pgPaymentGateway.cancelPayment(payment.getPgTid(), cancelReason);
            return cancelResult.success();
        } catch (Exception e) {
            return false;
        }
    }
}

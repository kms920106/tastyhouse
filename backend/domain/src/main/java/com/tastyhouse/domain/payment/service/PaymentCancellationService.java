package com.tastyhouse.domain.payment.service;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.model.Order;
import com.tastyhouse.domain.order.model.OrderStatus;
import com.tastyhouse.domain.order.service.OrderTransitionService;
import com.tastyhouse.domain.payment.event.PaymentCancelledEvent;
import com.tastyhouse.domain.payment.event.RefundRequestedEvent;
import com.tastyhouse.domain.payment.model.Payment;
import com.tastyhouse.domain.payment.model.PaymentCancelCode;
import com.tastyhouse.domain.payment.model.PaymentRefund;
import com.tastyhouse.domain.payment.model.PaymentStatus;
import com.tastyhouse.domain.payment.model.PgProvider;
import com.tastyhouse.domain.payment.repository.PaymentRefundRepository;
import com.tastyhouse.domain.payment.repository.PaymentRepository;
import com.tastyhouse.domain.payment.vo.Amount;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.domain.payment.vo.PaymentRefundId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
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
 * <p><b>PG 취소 요청은 이 서비스 안에서 하지 않는다</b> — 취소는 (1) 취소 가능 여부를 판정하는
 * {@link #prepareCancellation}(DB 읽기, 트랜잭션 안)과 (2) 결제·주문을 취소 전이하는
 * {@link #applyCancellation}(DB 쓰기, 별도 트랜잭션)으로 쪼개져 있고, 그 사이의 PG 취소 요청은 소비
 * 모듈이 <b>트랜잭션 밖에서</b> 수행한다. 과거에는 PG 왕복 전체가 DB 트랜잭션 안에 있어 커넥션·행 락을
 * 네트워크 지연만큼 점유했고, PG 취소 성공 후 커밋이 실패하면 "PG는 취소, DB는 미취소"가 되어 보상이
 * 불가능했다. 그래서 이 서비스는 {@code PgPaymentGateway}를 주입받지 않는다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code PaymentDomainConfig}가 담당한다. 트랜잭션 경계는 이 서비스를 호출하는
 * 소비 모듈의 command 서비스(web-api {@code PaymentCommandService})와 그 트랜잭션 경계 빈
 * ({@code PaymentCancellationExecutor})이 선언한다.
 *
 * <p>이벤트 발행은 Spring {@code ApplicationEventPublisher}가 아니라 프레임워크-프리 포트
 * {@link DomainEventPublisher}를 쓴다.
 *
 * <p>도메인 모델이 순수 POJO라 더티 체킹이 없으므로 전이 후 명시적으로 {@code save}를 호출한다.
 */
public class PaymentCancellationService {

    private final PaymentRepository paymentRepository;
    private final PaymentRefundRepository paymentRefundRepository;
    private final OrderTransitionService orderTransitionService;
    private final DomainEventPublisher domainEventPublisher;

    public PaymentCancellationService(
        PaymentRepository paymentRepository,
        PaymentRefundRepository paymentRefundRepository,
        OrderTransitionService orderTransitionService,
        DomainEventPublisher domainEventPublisher
    ) {
        this.paymentRepository = paymentRepository;
        this.paymentRefundRepository = paymentRefundRepository;
        this.orderTransitionService = orderTransitionService;
        this.domainEventPublisher = domainEventPublisher;
    }

    /**
     * 취소 1단 — PG 취소 요청 <b>전에</b> 소유권과 주문 상태로 취소 가능 여부를 판정하고, PG 취소가 필요한
     * 결제인지와 그 요청에 쓸 PG 거래 식별자를 확정해 돌려준다.
     *
     * <p>취소 불가 사유(조리 시작·이미 취소·주문 완료)는 예외가 아니라 {@link PaymentCancelCode}로 돌려준다 —
     * 호출자가 사유를 그대로 사용자에게 안내해야 하고, 이 경우 어떤 상태도 바꾸지 않기 때문이다(기존 동작
     * 보존). 소유권 위반만 예외({@code PAYMENT_ACCESS_DENIED})이며 판정 순서도 기존과 같다.
     *
     * <p>이 메서드는 읽기만 하므로 여기서 거절 코드가 나오거나 예외가 나면 바꿀 상태가 없다 — 즉
     * <b>PG를 호출하기 전에 거절되던 요청은 여전히 PG 호출 없이 같은 코드로 거절된다.</b>
     */
    public PaymentCancellationTarget prepareCancellation(MemberId memberId, PaymentId paymentId) {
        Payment payment = loadPayment(paymentId);
        Order order = orderTransitionService.loadOwnedBy(
            payment.getOrderId(), memberId, ErrorCode.PAYMENT_ACCESS_DENIED
        );

        PaymentCancelCode cancelCode = resolveCancelCode(order.getOrderStatus());
        if (cancelCode != PaymentCancelCode.SUCCESS) {
            return PaymentCancellationTarget.rejected(cancelCode);
        }

        return PaymentCancellationTarget.cancellable(isPgCancelRequired(payment), payment.getPgTid());
    }

    /**
     * 취소 2단 — PG 취소가 성공(또는 불필요)한 뒤 결제·주문을 함께 취소 전이하고 포인트 원복 이벤트를
     * 발행한다.
     *
     * <p>새 트랜잭션에서 결제·주문을 <b>다시 로드</b>한다(1단 인스턴스는 detached). PG 왕복 동안 주문이
     * 조리 시작·완료로 넘어갔을 수 있으므로 취소 가능 여부를 <b>한 번 더</b> 판정해, 그 경우 상태를 바꾸지
     * 않고 판정 코드를 그대로 돌려준다 — 1단에만 검사를 두면 그 경합이 열린다.
     *
     * <p><b>PG 취소는 이미 성공한 상태로 이 메서드에 들어온다.</b> 따라서 여기서 거절 코드가 나오거나 저장이
     * 실패하면 "PG는 취소, DB는 미취소"라는 보상 필요 상태가 되므로, 호출자가 그 상황을 감지해 운영 개입용
     * 로그를 남긴다({@code PaymentCancellationExecutor}/{@code PaymentCommandService} 참고).
     *
     * @return 취소 결과 코드 — {@link PaymentCancelCode#SUCCESS}면 취소가 반영됨
     */
    public PaymentCancelCode applyCancellation(MemberId memberId, PaymentId paymentId, String cancelReason) {
        Payment payment = loadPayment(paymentId);
        Order order = orderTransitionService.loadOwnedBy(
            payment.getOrderId(), memberId, ErrorCode.PAYMENT_ACCESS_DENIED
        );

        PaymentCancelCode cancelCode = resolveCancelCode(order.getOrderStatus());
        if (cancelCode != PaymentCancelCode.SUCCESS) {
            return cancelCode;
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
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PAYMENT_NOT_FOUND));
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
}

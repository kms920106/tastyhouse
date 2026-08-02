package com.tastyhouse.domain.payment.domain.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.payment.service.PaymentCancellationService;
import com.tastyhouse.domain.payment.service.PaymentCancellationTarget;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.model.Order;
import com.tastyhouse.domain.order.model.OrderStatus;
import com.tastyhouse.domain.order.repository.OrderRepository;
import com.tastyhouse.domain.order.service.OrderTransitionService;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.payment.event.PaymentCancelledEvent;
import com.tastyhouse.domain.payment.event.RefundRequestedEvent;
import com.tastyhouse.domain.payment.model.Payment;
import com.tastyhouse.domain.payment.model.PaymentCancelCode;
import com.tastyhouse.domain.payment.model.PaymentMethod;
import com.tastyhouse.domain.payment.model.PaymentRefund;
import com.tastyhouse.domain.payment.model.PaymentStatus;
import com.tastyhouse.domain.payment.model.PgProvider;
import com.tastyhouse.domain.payment.model.RefundStatus;
import com.tastyhouse.domain.payment.repository.PaymentRefundRepository;
import com.tastyhouse.domain.payment.repository.PaymentRepository;
import com.tastyhouse.domain.payment.vo.Amount;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 결제 취소·환불 도메인 서비스 단위 테스트.
 *
 * <p>순수 POJO(도메인 서비스)이므로 Spring 컨텍스트·JPA 없이 write 포트·PG 게이트웨이·이벤트 발행 포트를
 * 손으로 만든 스텁으로 대체해 검증한다.
 *
 * <p>핵심 검증 대상은 <b>결제 취소와 주문 취소가 항상 함께 반영되고, 취소 불가 시에는 어느 쪽도 바뀌지
 * 않는다</b>는 원자 불변식이다.
 *
 * <p><b>PG 취소 요청은 이 서비스 밖으로 나갔다</b>(P3 트랜잭션 경계 정리) — 취소가 사전 판정
 * {@code prepareCancellation}(읽기)과 결과 반영 {@code applyCancellation}(쓰기)으로 쪼개지고 그 사이의 PG
 * 호출은 소비 모듈이 트랜잭션 밖에서 수행한다. 따라서 여기서는 (1) 사전 판정이 PG 호출 필요 여부와 거절
 * 코드를 올바르게 돌려주는지, (2) 결과 반영이 결제·주문을 함께 취소하는지를 검증하고, "PG 실패 시
 * CANCEL_FAILED" 같은 오케스트레이션 동작은 소비 모듈의 {@code PaymentCommandService}가 책임진다.
 */
class PaymentCancellationServiceTest {

    private static final MemberId MEMBER_ID = MemberId.of(7L);
    private static final MemberId OTHER_MEMBER_ID = MemberId.of(99L);
    private static final OrderId ORDER_ID = OrderId.of(100L);
    private static final PaymentId PAYMENT_ID = PaymentId.of(200L);

    @Test
    @DisplayName("반영: 결제와 주문을 함께 취소 저장하고 포인트 원복 이벤트를 발행한다")
    void applyCancellation_cancelsPaymentAndOrderTogether() {
        Fixture fixture = Fixture.with(PaymentStatus.COMPLETED, OrderStatus.CONFIRMED);

        PaymentCancelCode code = fixture.service.applyCancellation(MEMBER_ID, PAYMENT_ID, "고객 변심");

        assertThat(code).isEqualTo(PaymentCancelCode.SUCCESS);
        assertThat(fixture.paymentRepository.lastSaved.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(fixture.paymentRepository.lastSaved.getCancelReason()).isEqualTo("고객 변심");
        assertThat(fixture.orderRepository.lastSaved.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);

        PaymentCancelledEvent event = (PaymentCancelledEvent) fixture.eventPublisher.published.getFirst();
        assertThat(event.usedPoint()).isEqualTo(500);
        assertThat(event.earnedPoint()).isEqualTo(300);
        assertThat(event.cancelReason()).isEqualTo("고객 변심");
    }

    @Test
    @DisplayName("사전 판정: 완료된 토스 결제는 PG 취소가 필요하다고 알리고 pgTid를 함께 돌려준다")
    void prepareCancellation_requiresPgCancelForCompletedTossPayment() {
        Fixture fixture = Fixture.with(PaymentStatus.COMPLETED, OrderStatus.CONFIRMED);

        PaymentCancellationTarget target = fixture.service.prepareCancellation(MEMBER_ID, PAYMENT_ID);

        assertThat(target.isRejected()).isFalse();
        assertThat(target.pgCancelRequired()).isTrue();
        assertThat(target.pgTid()).isEqualTo("tid-1");
    }

    @Test
    @DisplayName("사전 판정: 아직 승인되지 않은 결제는 PG 취소가 필요하지 않다")
    void prepareCancellation_skipsPgCancelForPendingPayment() {
        Fixture fixture = Fixture.with(PaymentStatus.PENDING, OrderStatus.PENDING);

        PaymentCancellationTarget target = fixture.service.prepareCancellation(MEMBER_ID, PAYMENT_ID);

        assertThat(target.isRejected()).isFalse();
        assertThat(target.pgCancelRequired()).isFalse();
    }

    @Test
    @DisplayName("사전 판정: 판정만 하므로 결제·주문을 저장하지 않는다(PG 호출 전이라 상태를 바꿀 수 없다)")
    void prepareCancellation_doesNotMutateState() {
        Fixture fixture = Fixture.with(PaymentStatus.COMPLETED, OrderStatus.CONFIRMED);

        fixture.service.prepareCancellation(MEMBER_ID, PAYMENT_ID);

        assertThat(fixture.paymentRepository.lastSaved).isNull();
        assertThat(fixture.orderRepository.lastSaved).isNull();
        assertThat(fixture.eventPublisher.published).isEmpty();
    }

    @Test
    @DisplayName("사전 판정 거절: 조리가 시작된 주문은 거절 코드만 돌려주고 결제·주문 모두 바꾸지 않는다")
    void prepareCancellation_rejectsWhenPreparing() {
        Fixture fixture = Fixture.with(PaymentStatus.COMPLETED, OrderStatus.PREPARING);

        PaymentCancellationTarget target = fixture.service.prepareCancellation(MEMBER_ID, PAYMENT_ID);

        assertThat(target.isRejected()).isTrue();
        assertThat(target.rejectCode()).isEqualTo(PaymentCancelCode.ALREADY_PREPARING);
        assertThat(target.pgCancelRequired()).isFalse();
        assertThat(fixture.paymentRepository.lastSaved).isNull();
        assertThat(fixture.orderRepository.lastSaved).isNull();
        assertThat(fixture.eventPublisher.published).isEmpty();
    }

    @Test
    @DisplayName("사전 판정 거절: 이미 취소·완료된 주문은 각각의 코드를 돌려준다")
    void prepareCancellation_rejectsWhenAlreadyCancelledOrCompleted() {
        assertThat(Fixture.with(PaymentStatus.CANCELLED, OrderStatus.CANCELLED).service
            .prepareCancellation(MEMBER_ID, PAYMENT_ID).rejectCode()).isEqualTo(PaymentCancelCode.ALREADY_CANCELLED);
        assertThat(Fixture.with(PaymentStatus.COMPLETED, OrderStatus.COMPLETED).service
            .prepareCancellation(MEMBER_ID, PAYMENT_ID).rejectCode()).isEqualTo(PaymentCancelCode.ORDER_COMPLETED);
    }

    @Test
    @DisplayName("반영 재판정: PG 취소 후 주문이 조리 시작으로 넘어갔으면 상태를 바꾸지 않고 거절 코드를 돌려준다")
    void applyCancellation_rejectsWhenOrderMovedDuringPgRoundTrip() {
        Fixture fixture = Fixture.with(PaymentStatus.COMPLETED, OrderStatus.PREPARING);

        PaymentCancelCode code = fixture.service.applyCancellation(MEMBER_ID, PAYMENT_ID, "고객 변심");

        assertThat(code).isEqualTo(PaymentCancelCode.ALREADY_PREPARING);
        assertThat(fixture.paymentRepository.lastSaved).isNull();
        assertThat(fixture.orderRepository.lastSaved).isNull();
        assertThat(fixture.eventPublisher.published).isEmpty();
    }

    @Test
    @DisplayName("거절: 다른 회원의 주문이면 사전 판정·반영 모두 PAYMENT_ACCESS_DENIED로 거절한다")
    void cancel_rejectsOtherMember() {
        Fixture prepareFixture = Fixture.with(PaymentStatus.COMPLETED, OrderStatus.CONFIRMED);
        assertThatThrownBy(() -> prepareFixture.service.prepareCancellation(OTHER_MEMBER_ID, PAYMENT_ID))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(ErrorCode.PAYMENT_ACCESS_DENIED.getDefaultMessage());

        Fixture applyFixture = Fixture.with(PaymentStatus.COMPLETED, OrderStatus.CONFIRMED);
        assertThatThrownBy(() -> applyFixture.service.applyCancellation(OTHER_MEMBER_ID, PAYMENT_ID, "사유"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(ErrorCode.PAYMENT_ACCESS_DENIED.getDefaultMessage());

        assertThat(applyFixture.paymentRepository.lastSaved).isNull();
        assertThat(applyFixture.orderRepository.lastSaved).isNull();
    }

    @Test
    @DisplayName("환불 요청: 완료된 결제에 대해 환불을 접수하고 요청 이벤트를 발행한다")
    void requestRefund_acceptsForCompletedPayment() {
        Fixture fixture = Fixture.with(PaymentStatus.COMPLETED, OrderStatus.CONFIRMED);

        fixture.service.requestRefund(MEMBER_ID, PAYMENT_ID, 5000, "일부 환불");

        assertThat(fixture.paymentRefundRepository.saved).hasSize(1);
        PaymentRefund refund = fixture.paymentRefundRepository.saved.getFirst();
        assertThat(refund.getRefundAmount().value()).isEqualTo(5000);
        assertThat(refund.getRefundStatus()).isEqualTo(RefundStatus.PENDING);

        RefundRequestedEvent event = (RefundRequestedEvent) fixture.eventPublisher.published.getFirst();
        assertThat(event.refundAmount().value()).isEqualTo(5000);
        assertThat(event.refundReason()).isEqualTo("일부 환불");
    }

    @Test
    @DisplayName("환불 요청: 완료되지 않은 결제는 환불할 수 없다")
    void requestRefund_rejectsNonCompletedPayment() {
        Fixture fixture = Fixture.with(PaymentStatus.PENDING, OrderStatus.PENDING);

        assertThatThrownBy(() -> fixture.service.requestRefund(MEMBER_ID, PAYMENT_ID, 5000, "사유"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(ErrorCode.PAYMENT_NOT_COMPLETED.getDefaultMessage());

        assertThat(fixture.paymentRefundRepository.saved).isEmpty();
    }

    @Test
    @DisplayName("환불 요청: 결제 금액을 초과하는 환불은 거절한다")
    void requestRefund_rejectsExceedingAmount() {
        Fixture fixture = Fixture.with(PaymentStatus.COMPLETED, OrderStatus.CONFIRMED);

        assertThatThrownBy(() -> fixture.service.requestRefund(MEMBER_ID, PAYMENT_ID, 21001, "사유"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(ErrorCode.PAYMENT_REFUND_AMOUNT_EXCEEDED.getDefaultMessage());

        assertThat(fixture.paymentRefundRepository.saved).isEmpty();
    }

    /**
     * 테스트 대상과 스텁 묶음 — 결제·주문 저장이 실제로 함께(또는 함께 안) 일어났는지 확인하기 위해 두
     * 리포지토리의 마지막 저장 값을 보관한다.
     */
    private static final class Fixture {

        private final PaymentCancellationService service;
        private final PaymentRepositoryStub paymentRepository;
        private final OrderRepositoryStub orderRepository;
        private final PaymentRefundRepositoryStub paymentRefundRepository;
        private final DomainEventPublisherStub eventPublisher;

        private Fixture(Payment payment, Order order) {
            this.paymentRepository = new PaymentRepositoryStub(payment);
            this.orderRepository = new OrderRepositoryStub(order);
            this.paymentRefundRepository = new PaymentRefundRepositoryStub();
            this.eventPublisher = new DomainEventPublisherStub();
            this.service = new PaymentCancellationService(
                paymentRepository,
                paymentRefundRepository,
                new OrderTransitionService(orderRepository),
                eventPublisher
            );
        }

        private static Fixture with(PaymentStatus paymentStatus, OrderStatus orderStatus) {
            return new Fixture(payment(paymentStatus), order(orderStatus));
        }

        private static Payment payment(PaymentStatus paymentStatus) {
            return Payment.reconstitute(
                PAYMENT_ID.value(), ORDER_ID, PaymentMethod.CREDIT_CARD, paymentStatus, new Amount(21000),
                PgProvider.TOSS, "tid-1", "pg-order-1", null, null, null,
                LocalDateTime.of(2026, 7, 31, 9, 30), null, null, null,
                LocalDateTime.of(2026, 7, 31, 9, 0)
            );
        }

        private static Order order(OrderStatus orderStatus) {
            return Order.reconstitute(
                ORDER_ID.value(), MEMBER_ID, ShopId.of(1L), "ORD-1", null, orderStatus,
                "주문자", "01012345678", "orderer@tastyhouse.com",
                21000, 0, 0, 500, 500, 21000, null, 500, 300,
                false, LocalDateTime.of(2026, 7, 31, 9, 0), LocalDateTime.of(2026, 7, 31, 9, 0)
            );
        }
    }

    private static final class PaymentRepositoryStub implements PaymentRepository {

        private final Payment stored;
        private Payment lastSaved;

        private PaymentRepositoryStub(Payment stored) {
            this.stored = stored;
        }

        @Override
        public Optional<Payment> findById(PaymentId paymentId) {
            return Optional.of(stored);
        }

        @Override
        public Optional<Payment> findByPgOrderId(String pgOrderId) {
            return Optional.of(stored);
        }

        @Override
        public boolean existsByOrderId(OrderId orderId) {
            return true;
        }

        @Override
        public Payment save(Payment payment) {
            this.lastSaved = payment;
            return payment;
        }
    }

    private static final class OrderRepositoryStub implements OrderRepository {

        private final Order stored;
        private Order lastSaved;

        private OrderRepositoryStub(Order stored) {
            this.stored = stored;
        }

        @Override
        public Optional<Order> findById(OrderId orderId) {
            return Optional.of(stored);
        }

        @Override
        public Order save(Order order) {
            this.lastSaved = order;
            return order;
        }
    }

    private static final class PaymentRefundRepositoryStub implements PaymentRefundRepository {

        private final List<PaymentRefund> saved = new ArrayList<>();

        @Override
        public PaymentRefund save(PaymentRefund paymentRefund) {
            saved.add(PaymentRefund.reconstitute(
                300L,
                paymentRefund.getPaymentId(),
                paymentRefund.getRefundAmount(),
                paymentRefund.getRefundReason(),
                paymentRefund.getRefundStatus(),
                paymentRefund.getPgRefundId(),
                paymentRefund.getRefundedAt(),
                LocalDateTime.of(2026, 7, 31, 10, 0)
            ));
            return saved.getLast();
        }
    }

    private static final class DomainEventPublisherStub implements DomainEventPublisher {

        private final List<Object> published = new ArrayList<>();

        @Override
        public void publish(Object event) {
            published.add(event);
        }
    }
}

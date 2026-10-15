package com.tastyhouse.application.payment.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.model.Order;
import com.tastyhouse.domain.order.model.OrderStatus;
import com.tastyhouse.domain.order.vo.OrderDeliveryDestination;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.order.vo.OrderSchedule;
import com.tastyhouse.domain.payment.event.PaymentCancelledEvent;
import com.tastyhouse.domain.payment.event.RefundRequestedEvent;
import com.tastyhouse.domain.payment.model.Payment;
import com.tastyhouse.domain.payment.model.PaymentCancelCode;
import com.tastyhouse.domain.payment.model.PaymentCancellationTarget;
import com.tastyhouse.domain.payment.model.PaymentMethod;
import com.tastyhouse.domain.payment.model.PaymentRefund;
import com.tastyhouse.domain.payment.model.PaymentStatus;
import com.tastyhouse.domain.payment.model.PgProvider;
import com.tastyhouse.domain.payment.model.RefundStatus;
import com.tastyhouse.domain.payment.vo.Amount;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.order.port.out.write.OrderLoadPort;
import com.tastyhouse.application.order.port.out.write.OrderSavePort;
import com.tastyhouse.application.order.service.OrderTransitionService;
import com.tastyhouse.application.payment.port.out.write.PaymentLoadPort;
import com.tastyhouse.application.payment.port.out.write.PaymentRefundLoadPort;
import com.tastyhouse.application.payment.port.out.write.PaymentRefundSavePort;
import com.tastyhouse.application.payment.port.out.write.PaymentSavePort;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.WebErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        assertThat(fixture.paymentPersistence.lastSaved.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(fixture.paymentPersistence.lastSaved.getCancelReason()).isEqualTo("고객 변심");
        assertThat(fixture.orderPersistence.lastSaved.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);

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
        assertThat(target.pgProvider()).isEqualTo(PgProvider.TOSS);
        assertThat(target.pgTid()).isEqualTo("tid-1");
    }

    @Test
    @DisplayName("사전 판정: 토스 외 PG로 완료된 결제도 PG 취소 대상으로 판정하고 담당 PG를 함께 돌려준다")
    void prepareCancellation_carriesProviderForNonTossPayment() {
        Fixture fixture = Fixture.with(PgProvider.KAKAO, PaymentStatus.COMPLETED, OrderStatus.CONFIRMED);

        PaymentCancellationTarget target = fixture.service.prepareCancellation(MEMBER_ID, PAYMENT_ID);

        assertThat(target.isRejected()).isFalse();
        assertThat(target.pgCancelRequired()).isTrue();
        assertThat(target.pgProvider()).isEqualTo(PgProvider.KAKAO);
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

        assertThat(fixture.paymentPersistence.lastSaved).isNull();
        assertThat(fixture.orderPersistence.lastSaved).isNull();
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
        assertThat(fixture.paymentPersistence.lastSaved).isNull();
        assertThat(fixture.orderPersistence.lastSaved).isNull();
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
        assertThat(fixture.paymentPersistence.lastSaved).isNull();
        assertThat(fixture.orderPersistence.lastSaved).isNull();
        assertThat(fixture.eventPublisher.published).isEmpty();
    }

    @Test
    @DisplayName("거절: 다른 회원의 주문이면 사전 판정·반영 모두 PAYMENT_ACCESS_DENIED로 거절한다")
    void cancel_rejectsOtherMember() {
        Fixture prepareFixture = Fixture.with(PaymentStatus.COMPLETED, OrderStatus.CONFIRMED);
        assertThatThrownBy(() -> prepareFixture.service.prepareCancellation(OTHER_MEMBER_ID, PAYMENT_ID))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(ApplicationErrorCode.PAYMENT_ACCESS_DENIED.getDefaultMessage());

        Fixture applyFixture = Fixture.with(PaymentStatus.COMPLETED, OrderStatus.CONFIRMED);
        assertThatThrownBy(() -> applyFixture.service.applyCancellation(OTHER_MEMBER_ID, PAYMENT_ID, "사유"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(ApplicationErrorCode.PAYMENT_ACCESS_DENIED.getDefaultMessage());

        assertThat(applyFixture.paymentPersistence.lastSaved).isNull();
        assertThat(applyFixture.orderPersistence.lastSaved).isNull();
    }

    @Test
    @DisplayName("환불 요청: 완료된 결제에 대해 환불을 접수하고 요청 이벤트를 발행한다")
    void requestRefund_acceptsForCompletedPayment() {
        Fixture fixture = Fixture.with(PaymentStatus.COMPLETED, OrderStatus.PREPARING);

        fixture.service.requestRefund(MEMBER_ID, PAYMENT_ID, 5000, "일부 환불");

        assertThat(fixture.paymentRefundPersistence.saved).hasSize(1);
        PaymentRefund refund = fixture.paymentRefundPersistence.saved.getFirst();
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
            .hasMessageContaining(WebErrorCode.PAYMENT_NOT_COMPLETED.getDefaultMessage());

        assertThat(fixture.paymentRefundPersistence.saved).isEmpty();
    }

    @Test
    @DisplayName("환불 요청: 결제 금액을 초과하는 환불은 거절한다")
    void requestRefund_rejectsExceedingAmount() {
        Fixture fixture = Fixture.with(PaymentStatus.COMPLETED, OrderStatus.PREPARING);

        assertThatThrownBy(() -> fixture.service.requestRefund(MEMBER_ID, PAYMENT_ID, 21001, "사유"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(WebErrorCode.PAYMENT_REFUND_AMOUNT_EXCEEDED.getDefaultMessage());

        assertThat(fixture.paymentRefundPersistence.saved).isEmpty();
    }

    @Test
    @DisplayName("환불 요청: 결제 행을 잠금 조회로 읽어 같은 결제의 동시 요청을 직렬화한다")
    void requestRefund_loadsPaymentWithLock() {
        Fixture fixture = Fixture.with(PaymentStatus.COMPLETED, OrderStatus.PREPARING);

        fixture.service.requestRefund(MEMBER_ID, PAYMENT_ID, 5000, "일부 환불");

        assertThat(fixture.paymentRefundPersistence.lockedLoadsAtFirstSumQuery).isEqualTo(1);
        assertThat(fixture.orderPersistence.lockedLoadsAtFirstOrderLoad).isEqualTo(1);
        assertThat(fixture.paymentPersistence.lockedLoads).isEqualTo(1);
        assertThat(fixture.paymentPersistence.plainLoads).isZero();
    }

    @Test
    @DisplayName("환불 요청: 같은 결제에 전액 환불을 연달아 요청하면 두 번째 요청은 거절한다")
    void requestRefund_rejectsRepeatedFullRefund() {
        Fixture fixture = Fixture.with(PaymentStatus.COMPLETED, OrderStatus.PREPARING);

        fixture.service.requestRefund(MEMBER_ID, PAYMENT_ID, 21000, "전액 환불");

        assertThatThrownBy(() -> fixture.service.requestRefund(MEMBER_ID, PAYMENT_ID, 21000, "전액 환불"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(WebErrorCode.PAYMENT_REFUND_AMOUNT_EXCEEDED.getDefaultMessage());

        assertThat(fixture.paymentRefundPersistence.saved).hasSize(1);
    }

    @Test
    @DisplayName("환불 요청: 이미 접수된 환불과 합친 금액이 결제 금액을 넘으면 거절한다")
    void requestRefund_rejectsWhenCumulativeAmountExceeds() {
        Fixture fixture = Fixture.with(PaymentStatus.COMPLETED, OrderStatus.PREPARING);
        fixture.paymentRefundPersistence.existing(15000, RefundStatus.PENDING);

        assertThatThrownBy(() -> fixture.service.requestRefund(MEMBER_ID, PAYMENT_ID, 7000, "추가 환불"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(WebErrorCode.PAYMENT_REFUND_AMOUNT_EXCEEDED.getDefaultMessage());

        assertThat(fixture.paymentRefundPersistence.saved).isEmpty();
        assertThat(fixture.eventPublisher.published).isEmpty();
    }

    @Test
    @DisplayName("환불 요청: 누적 금액이 결제 금액과 정확히 같으면 접수한다")
    void requestRefund_acceptsWhenCumulativeAmountEqualsPayment() {
        Fixture fixture = Fixture.with(PaymentStatus.COMPLETED, OrderStatus.PREPARING);
        fixture.paymentRefundPersistence.existing(15000, RefundStatus.PENDING);

        fixture.service.requestRefund(MEMBER_ID, PAYMENT_ID, 6000, "잔액 환불");

        assertThat(fixture.paymentRefundPersistence.saved).hasSize(1);
    }

    @Test
    @DisplayName("환불 요청: 처리 중·완료된 환불도 누적 금액에 포함한다")
    void requestRefund_countsProcessingAndCompletedRefunds() {
        Fixture fixture = Fixture.with(PaymentStatus.COMPLETED, OrderStatus.COMPLETED);
        fixture.paymentRefundPersistence.existing(10000, RefundStatus.PROCESSING);
        fixture.paymentRefundPersistence.existing(10000, RefundStatus.COMPLETED);

        assertThatThrownBy(() -> fixture.service.requestRefund(MEMBER_ID, PAYMENT_ID, 1001, "추가 환불"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(WebErrorCode.PAYMENT_REFUND_AMOUNT_EXCEEDED.getDefaultMessage());

        assertThat(fixture.paymentRefundPersistence.saved).isEmpty();
    }

    @Test
    @DisplayName("환불 요청: 실패한 환불은 한도를 점유하지 않아 누적 금액에서 뺀다")
    void requestRefund_excludesFailedRefundsFromCumulativeAmount() {
        Fixture fixture = Fixture.with(PaymentStatus.COMPLETED, OrderStatus.PREPARING);
        fixture.paymentRefundPersistence.existing(21000, RefundStatus.FAILED);

        fixture.service.requestRefund(MEMBER_ID, PAYMENT_ID, 21000, "재요청");

        assertThat(fixture.paymentRefundPersistence.saved).hasSize(1);
    }

    @Test
    @DisplayName("환불 요청: 조리 시작 전이거나 취소된 주문은 환불 요청을 거절하고 아무것도 저장하지 않는다")
    void requestRefund_rejectsOrderStatusBeforePreparingOrCancelled() {
        for (OrderStatus orderStatus : List.of(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.CANCELLED)) {
            Fixture fixture = Fixture.with(PaymentStatus.COMPLETED, orderStatus);

            assertThatThrownBy(() -> fixture.service.requestRefund(MEMBER_ID, PAYMENT_ID, 5000, "사유"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(WebErrorCode.PAYMENT_REFUND_ORDER_STATUS_INVALID.getDefaultMessage());

            assertThat(fixture.paymentRefundPersistence.saved).isEmpty();
            assertThat(fixture.eventPublisher.published).isEmpty();
        }
    }

    @Test
    @DisplayName("환불 요청: 완료된 주문은 환불 요청을 접수한다")
    void requestRefund_acceptsForCompletedOrder() {
        Fixture fixture = Fixture.with(PaymentStatus.COMPLETED, OrderStatus.COMPLETED);

        fixture.service.requestRefund(MEMBER_ID, PAYMENT_ID, 5000, "사유");

        assertThat(fixture.paymentRefundPersistence.saved).hasSize(1);
    }

    private static final class Fixture {

        private final PaymentCancellationService service;
        private final FakePaymentPersistence paymentPersistence;
        private final FakeOrderPersistence orderPersistence;
        private final FakePaymentRefundPersistence paymentRefundPersistence;
        private final DomainEventPublisherStub eventPublisher;

        private Fixture(Payment payment, Order order) {
            this.paymentPersistence = new FakePaymentPersistence(payment);
            this.orderPersistence = new FakeOrderPersistence(order, paymentPersistence);
            this.paymentRefundPersistence = new FakePaymentRefundPersistence(paymentPersistence);
            this.eventPublisher = new DomainEventPublisherStub();
            this.service = new PaymentCancellationService(
                paymentPersistence,
                paymentPersistence,
                paymentRefundPersistence,
                paymentRefundPersistence,
                new OrderTransitionService(orderPersistence, orderPersistence),
                eventPublisher
            );
        }

        private static Fixture with(PaymentStatus paymentStatus, OrderStatus orderStatus) {
            return with(PgProvider.TOSS, paymentStatus, orderStatus);
        }

        private static Fixture with(PgProvider pgProvider, PaymentStatus paymentStatus, OrderStatus orderStatus) {
            return new Fixture(payment(pgProvider, paymentStatus), order(orderStatus));
        }

        private static Payment payment(PgProvider pgProvider, PaymentStatus paymentStatus) {
            return Payment.reconstitute(
                PAYMENT_ID.value(), ORDER_ID, PaymentMethod.CREDIT_CARD, paymentStatus, new Amount(21000),
                pgProvider, "tid-1", "pg-order-1", null, null, null,
                LocalDateTime.of(2026, 7, 31, 9, 30), null, null, null,
                LocalDateTime.of(2026, 7, 31, 9, 0)
            );
        }

        private static Order order(OrderStatus orderStatus) {
            return Order.reconstitute(
                ORDER_ID.value(), MEMBER_ID, ShopId.of(1L), "ORD-1", null, orderStatus,
                "주문자", "01012345678", "orderer@tastyhouse.com",
                21000, 0, 0, 500, 500, 0, 0, 21000, OrderDeliveryDestination.none(), OrderSchedule.none(), null, 500, 300,
                false, LocalDateTime.of(2026, 7, 31, 9, 0), LocalDateTime.of(2026, 7, 31, 9, 0)
            );
        }
    }

    private static final class FakePaymentPersistence implements PaymentLoadPort, PaymentSavePort {

        private final Payment stored;
        private Payment lastSaved;
        private int lockedLoads;
        private int plainLoads;

        private FakePaymentPersistence(Payment stored) {
            this.stored = stored;
        }

        @Override
        public Optional<Payment> findById(PaymentId paymentId) {
            plainLoads++;
            return Optional.of(stored);
        }

        @Override
        public Optional<Payment> findByIdForUpdate(PaymentId paymentId) {
            lockedLoads++;
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

    private static final class FakeOrderPersistence implements OrderLoadPort, OrderSavePort {

        private final Order stored;
        private final FakePaymentPersistence paymentPersistence;
        private Order lastSaved;
        private Integer lockedLoadsAtFirstOrderLoad;

        private FakeOrderPersistence(Order stored, FakePaymentPersistence paymentPersistence) {
            this.stored = stored;
            this.paymentPersistence = paymentPersistence;
        }

        @Override
        public Optional<Order> findByIdIncludingDeleted(OrderId orderId) {
            if (lockedLoadsAtFirstOrderLoad == null) {
                lockedLoadsAtFirstOrderLoad = paymentPersistence.lockedLoads;
            }
            return Optional.of(stored);
        }

        @Override
        public Order save(Order order) {
            this.lastSaved = order;
            return order;
        }
    }

    private static final class FakePaymentRefundPersistence implements PaymentRefundLoadPort, PaymentRefundSavePort {

        private final FakePaymentPersistence paymentPersistence;
        private final List<PaymentRefund> existing = new ArrayList<>();
        private final List<PaymentRefund> saved = new ArrayList<>();
        private Integer lockedLoadsAtFirstSumQuery;

        private FakePaymentRefundPersistence(FakePaymentPersistence paymentPersistence) {
            this.paymentPersistence = paymentPersistence;
        }

        private void existing(int amount, RefundStatus status) {
            existing.add(PaymentRefund.reconstitute(
                (long) existing.size() + 1, PAYMENT_ID, new Amount(amount), "기존 환불", status, null, null,
                LocalDateTime.of(2026, 7, 31, 9, 45)
            ));
        }

        @Override
        public long findRefundAmountSum(PaymentId paymentId, Set<RefundStatus> statuses) {
            if (lockedLoadsAtFirstSumQuery == null) {
                lockedLoadsAtFirstSumQuery = paymentPersistence.lockedLoads;
            }
            return existing.stream()
                .filter(refund -> refund.getPaymentId().equals(paymentId))
                .filter(refund -> statuses.contains(refund.getRefundStatus()))
                .mapToLong(refund -> refund.getRefundAmount().value())
                .sum();
        }

        @Override
        public PaymentRefund save(PaymentRefund paymentRefund) {
            existing.add(paymentRefund);
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

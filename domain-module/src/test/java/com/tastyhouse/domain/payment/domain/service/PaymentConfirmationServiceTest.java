package com.tastyhouse.domain.payment.domain.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.order.domain.model.Order;
import com.tastyhouse.domain.order.domain.model.OrderStatus;
import com.tastyhouse.domain.order.domain.repository.OrderRepository;
import com.tastyhouse.domain.order.domain.service.OrderTransitionService;
import com.tastyhouse.domain.order.domain.vo.OrderId;
import com.tastyhouse.domain.shop.domain.vo.ShopId;
import com.tastyhouse.domain.payment.domain.event.PaymentCompletedEvent;
import com.tastyhouse.domain.payment.domain.model.Payment;
import com.tastyhouse.domain.payment.domain.model.PaymentMethod;
import com.tastyhouse.domain.payment.domain.model.PaymentStatus;
import com.tastyhouse.domain.payment.domain.model.PgProvider;
import com.tastyhouse.domain.payment.domain.model.TossPaymentRecord;
import com.tastyhouse.domain.payment.domain.port.dto.PgConfirmResult;
import com.tastyhouse.domain.payment.domain.port.dto.TossPaymentDetail;
import com.tastyhouse.domain.payment.domain.repository.PaymentRepository;
import com.tastyhouse.domain.payment.domain.repository.TossPaymentRecordRepository;
import com.tastyhouse.domain.payment.domain.vo.Amount;
import com.tastyhouse.domain.payment.domain.vo.PaymentId;
import com.tastyhouse.domain.exception.AccessDeniedException;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 결제 개시·승인 도메인 서비스 단위 테스트.
 *
 * <p>순수 POJO(도메인 서비스)이므로 Spring 컨텍스트·JPA 없이 write 포트·PG 게이트웨이·이벤트 발행 포트를
 * 손으로 만든 스텁으로 대체해 검증한다.
 *
 * <p>핵심 검증 대상은 <b>결제 완료와 주문 확정이 항상 함께 반영된다</b>는 원자 불변식이다 — 승인 경로가
 * 세 가지(PG 콜백·토스 승인·현장결제)여도 규칙이 갈리지 않아야 한다.
 */
class PaymentConfirmationServiceTest {

    private static final MemberId MEMBER_ID = MemberId.of(7L);
    private static final MemberId OTHER_MEMBER_ID = MemberId.of(99L);
    private static final OrderId ORDER_ID = OrderId.of(100L);
    private static final PaymentId PAYMENT_ID = PaymentId.of(200L);

    @Test
    @DisplayName("PG 콜백 승인: 결제를 완료 전이하고 주문을 확정해 둘 다 저장한다")
    void confirm_completesPaymentAndConfirmsOrder() {
        Fixture fixture = Fixture.withPendingPayment(OrderStatus.PENDING);

        PaymentId confirmed = fixture.service.confirm(PAYMENT_ID, PgConfirmation.of(
            PgProvider.TOSS, "tid-1", "pg-order-1", "신한카드", "1234-****", 0, "https://receipt"
        ));

        assertThat(confirmed.value()).isEqualTo(PAYMENT_ID.value());
        assertThat(fixture.paymentRepository.lastSaved.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(fixture.paymentRepository.lastSaved.getPgTid()).isEqualTo("tid-1");
        assertThat(fixture.paymentRepository.lastSaved.getCardCompany()).isEqualTo("신한카드");
        assertThat(fixture.orderRepository.lastSaved.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("PG 콜백 승인: 승인 대기 상태가 아니면 결제도 주문도 바뀌지 않는다")
    void confirm_rejectsWhenNotPending() {
        Fixture fixture = Fixture.withCompletedPayment();

        assertThatThrownBy(() -> fixture.service.confirm(PAYMENT_ID, PgConfirmation.of(
            PgProvider.TOSS, "tid-1", "pg-order-1", null, null, null, null
        )))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(ErrorCode.PAYMENT_NOT_PENDING_APPROVAL.getDefaultMessage());

        assertThat(fixture.paymentRepository.lastSaved).isNull();
        assertThat(fixture.orderRepository.lastSaved).isNull();
    }

    @Test
    @DisplayName("토스 반영(성공): 결제 완료·주문 확정·원장 기록·완료 이벤트가 모두 일어난다")
    void applyTossConfirmation_completesAndPublishesEvent() {
        Fixture fixture = Fixture.withPendingPayment(OrderStatus.PENDING);

        fixture.service.applyTossConfirmation(MEMBER_ID, "pg-order-1", successConfirmResult());

        assertThat(fixture.paymentRepository.lastSaved.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(fixture.orderRepository.lastSaved.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(fixture.tossPaymentRecordRepository.saved).hasSize(1);
        assertThat(fixture.eventPublisher.published).hasSize(1);

        PaymentCompletedEvent event = (PaymentCompletedEvent) fixture.eventPublisher.published.getFirst();
        assertThat(event.isOnSitePayment()).isFalse();
        assertThat(event.memberId()).isEqualTo(MEMBER_ID);
    }

    @Test
    @DisplayName("토스 반영(실패): 결제를 FAILED로 저장하고 주문은 확정하지 않으며 원장은 남긴다")
    void failTossConfirmation_savesFailedWithoutConfirmingOrder() {
        Fixture fixture = Fixture.withPendingPayment(OrderStatus.PENDING);
        PgConfirmResult rejected = new PgConfirmResult(
            false, null, null, null, null, null, null, null, null, "REJECT", "한도 초과", detail()
        );

        fixture.service.failTossConfirmation("pg-order-1", rejected);

        assertThat(fixture.paymentRepository.lastSaved.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(fixture.orderRepository.lastSaved).isNull();
        assertThat(fixture.tossPaymentRecordRepository.saved).hasSize(1);
        assertThat(fixture.eventPublisher.published).isEmpty();
    }

    @Test
    @DisplayName("토스 반영(실패): 예외를 던지지 않는다 — 실패 근거(원장·FAILED 전이)가 커밋되어야 하므로 예외 변환은 호출자 몫이다")
    void failTossConfirmation_doesNotThrow() {
        Fixture fixture = Fixture.withPendingPayment(OrderStatus.PENDING);
        PgConfirmResult rejected = new PgConfirmResult(
            false, null, null, null, null, null, null, null, null, "REJECT", "한도 초과", detail()
        );

        fixture.service.failTossConfirmation("pg-order-1", rejected);

        assertThat(fixture.tossPaymentRecordRepository.saved).hasSize(1);
    }

    @Test
    @DisplayName("토스 사전 검증: 요청 금액이 결제 금액과 다르면 거절하고 아무 상태도 바꾸지 않는다(PG 호출 전)")
    void prepareTossConfirmation_rejectsAmountMismatch() {
        Fixture fixture = Fixture.withPendingPayment(OrderStatus.PENDING);

        assertThatThrownBy(() -> fixture.service.prepareTossConfirmation(MEMBER_ID, "pg-order-1", 999))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(ErrorCode.PAYMENT_AMOUNT_MISMATCH.getDefaultMessage());

        assertThat(fixture.paymentRepository.lastSaved).isNull();
        assertThat(fixture.orderRepository.lastSaved).isNull();
        assertThat(fixture.tossPaymentRecordRepository.saved).isEmpty();
    }

    @Test
    @DisplayName("토스 사전 검증: 통과하면 PG 요청에 쓸 결제 식별자·주문번호·금액을 확정해 돌려준다")
    void prepareTossConfirmation_returnsPgRequestTarget() {
        Fixture fixture = Fixture.withPendingPayment(OrderStatus.PENDING);

        TossConfirmationTarget target = fixture.service.prepareTossConfirmation(MEMBER_ID, "pg-order-1", 21000);

        assertThat(target.paymentId()).isEqualTo(PAYMENT_ID.value());
        assertThat(target.pgOrderId()).isEqualTo("pg-order-1");
        assertThat(target.amount()).isEqualTo(21000);
        assertThat(fixture.paymentRepository.lastSaved).isNull();
    }

    @Test
    @DisplayName("토스 승인: 다른 회원의 주문이면 사전 검증·반영 모두 PAYMENT_ACCESS_DENIED로 거절한다")
    void confirmTossPayment_rejectsOtherMember() {
        Fixture prepareFixture = Fixture.withPendingPayment(OrderStatus.PENDING);
        assertThatThrownBy(() -> prepareFixture.service.prepareTossConfirmation(OTHER_MEMBER_ID, "pg-order-1", 21000))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining(ErrorCode.PAYMENT_ACCESS_DENIED.getDefaultMessage());

        Fixture applyFixture = Fixture.withPendingPayment(OrderStatus.PENDING);
        assertThatThrownBy(() -> applyFixture.service.applyTossConfirmation(OTHER_MEMBER_ID, "pg-order-1", successConfirmResult()))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining(ErrorCode.PAYMENT_ACCESS_DENIED.getDefaultMessage());

        assertThat(applyFixture.paymentRepository.lastSaved).isNull();
        assertThat(applyFixture.orderRepository.lastSaved).isNull();
    }

    @Test
    @DisplayName("현장결제 완료: 결제를 완료하고 적립 포인트를 주문에 반영한 뒤 주문을 확정한다")
    void completeOnSitePayment_appliesEarnedPointAndConfirmsOrder() {
        Fixture fixture = Fixture.withPendingPayment(OrderStatus.PENDING, PaymentMethod.CASH_ON_SITE);

        fixture.service.completeOnSitePayment(MEMBER_ID, PAYMENT_ID);

        assertThat(fixture.paymentRepository.lastSaved.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(fixture.orderRepository.lastSaved.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
        // 21000원의 10% = 2100 — 이벤트 리스너의 실제 적립액과 같은 계산식을 쓴다.
        assertThat(fixture.orderRepository.lastSaved.getEarnedPoint()).isEqualTo(2100);

        PaymentCompletedEvent event = (PaymentCompletedEvent) fixture.eventPublisher.published.getFirst();
        assertThat(event.isOnSitePayment()).isTrue();
    }

    @Test
    @DisplayName("현장결제 완료: 현장결제 수단이 아니면 거절한다")
    void completeOnSitePayment_rejectsNonOnSiteMethod() {
        Fixture fixture = Fixture.withPendingPayment(OrderStatus.PENDING, PaymentMethod.CREDIT_CARD);

        assertThatThrownBy(() -> fixture.service.completeOnSitePayment(MEMBER_ID, PAYMENT_ID))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(ErrorCode.PAYMENT_NOT_ON_SITE.getDefaultMessage());

        assertThat(fixture.paymentRepository.lastSaved).isNull();
        assertThat(fixture.orderRepository.lastSaved).isNull();
    }

    @Test
    @DisplayName("적립 포인트 계산은 결제 금액의 10%로, 주문 반영과 실제 적립이 같은 식을 쓴다")
    void calculateEarnedPoint_isTenPercent() {
        Fixture fixture = Fixture.withPendingPayment(OrderStatus.PENDING);

        assertThat(fixture.service.earnRate()).isEqualTo(10);
        assertThat(fixture.service.calculateEarnedPoint(new Amount(21000))).isEqualTo(2100);
        assertThat(fixture.service.calculateEarnedPoint(new Amount(15))).isEqualTo(1);
    }

    @Test
    @DisplayName("결제 개시: 주문이 결제 대기 상태가 아니면 거절한다")
    void open_rejectsNonPendingOrder() {
        Fixture fixture = Fixture.withPendingPayment(OrderStatus.CONFIRMED);

        assertThatThrownBy(() -> fixture.service.open(MEMBER_ID, ORDER_ID, PaymentMethod.CREDIT_CARD))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(ErrorCode.PAYMENT_INVALID_ORDER_STATUS.getDefaultMessage());
    }

    @Test
    @DisplayName("결제 개시: 이미 결제가 진행 중인 주문이면 거절한다")
    void open_rejectsDuplicatePayment() {
        Fixture fixture = Fixture.withPendingPayment(OrderStatus.PENDING);
        fixture.paymentRepository.existsByOrderId = true;

        assertThatThrownBy(() -> fixture.service.open(MEMBER_ID, ORDER_ID, PaymentMethod.CREDIT_CARD))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(ErrorCode.PAYMENT_ALREADY_IN_PROGRESS.getDefaultMessage());
    }

    private static PgConfirmResult successConfirmResult() {
        return new PgConfirmResult(
            true, "payment-key", "DONE", 21000, LocalDateTime.of(2026, 7, 31, 10, 0),
            "https://receipt", "신한카드", "1234-****", 0, null, null, detail()
        );
    }

    private static TossPaymentDetail detail() {
        return new TossPaymentDetail(
            "2022-11-16", "payment-key", "NORMAL", "pg-order-1", "주문", "mid", "KRW", "카드",
            21000, 21000, "DONE", null, null, false, "tx-key", null, null, false, null, null,
            false, 21000, null, null, "1234-****", 0, null, false, null, null, null, false, null,
            null, null, null, null, null, null, false, null, null, null, null, null, null, null,
            null, null, "https://receipt", null, null, null, "KR"
        );
    }

    /**
     * 테스트 대상과 스텁 묶음 — 결제·주문 저장이 실제로 함께 일어났는지 확인하기 위해 두 리포지토리의
     * 마지막 저장 값을 보관한다.
     */
    private static final class Fixture {

        private final PaymentConfirmationService service;
        private final PaymentRepositoryStub paymentRepository;
        private final OrderRepositoryStub orderRepository;
        private final TossPaymentRecordRepositoryStub tossPaymentRecordRepository;
        private final DomainEventPublisherStub eventPublisher;

        private Fixture(Payment payment, Order order) {
            this.paymentRepository = new PaymentRepositoryStub(payment);
            this.orderRepository = new OrderRepositoryStub(order);
            this.tossPaymentRecordRepository = new TossPaymentRecordRepositoryStub();
            this.eventPublisher = new DomainEventPublisherStub();
            this.service = new PaymentConfirmationService(
                paymentRepository,
                tossPaymentRecordRepository,
                new OrderTransitionService(orderRepository),
                eventPublisher
            );
        }

        private static Fixture withPendingPayment(OrderStatus orderStatus) {
            return withPendingPayment(orderStatus, PaymentMethod.CREDIT_CARD);
        }

        private static Fixture withPendingPayment(OrderStatus orderStatus, PaymentMethod paymentMethod) {
            return new Fixture(payment(PaymentStatus.PENDING, paymentMethod), order(orderStatus));
        }

        private static Fixture withCompletedPayment() {
            return new Fixture(payment(PaymentStatus.COMPLETED, PaymentMethod.CREDIT_CARD), order(OrderStatus.CONFIRMED));
        }

        private static Payment payment(PaymentStatus paymentStatus, PaymentMethod paymentMethod) {
            return Payment.reconstitute(
                PAYMENT_ID.value(), ORDER_ID, paymentMethod, paymentStatus, new Amount(21000),
                PgProvider.TOSS, null, "pg-order-1", null, null, null, null, null, null, null,
                LocalDateTime.of(2026, 7, 31, 9, 0)
            );
        }

        private static Order order(OrderStatus orderStatus) {
            return Order.reconstitute(
                ORDER_ID.value(), MEMBER_ID, ShopId.of(1L), "ORD-1", null, orderStatus,
                "주문자", "01012345678", "orderer@tastyhouse.com",
                21000, 0, 0, 0, 0, 21000, null, 0, 0,
                false, LocalDateTime.of(2026, 7, 31, 9, 0), LocalDateTime.of(2026, 7, 31, 9, 0)
            );
        }
    }

    private static final class PaymentRepositoryStub implements PaymentRepository {

        private final Payment stored;
        private Payment lastSaved;
        private boolean existsByOrderId;

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
            return existsByOrderId;
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

    private static final class TossPaymentRecordRepositoryStub implements TossPaymentRecordRepository {

        private final List<TossPaymentRecord> saved = new ArrayList<>();

        @Override
        public TossPaymentRecord save(TossPaymentRecord tossPaymentRecord) {
            saved.add(tossPaymentRecord);
            return tossPaymentRecord;
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

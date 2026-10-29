package com.tastyhouse.application.payment.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.model.Order;
import com.tastyhouse.domain.order.model.OrderStatus;
import com.tastyhouse.domain.order.vo.OrderDeliveryDestination;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.order.vo.OrderSchedule;
import com.tastyhouse.domain.payment.event.PaymentCompletedEvent;
import com.tastyhouse.domain.payment.model.Payment;
import com.tastyhouse.domain.payment.model.PaymentMethod;
import com.tastyhouse.domain.payment.model.PaymentStatus;
import com.tastyhouse.domain.payment.model.PgProvider;
import com.tastyhouse.domain.payment.model.TossPaymentRecord;
import com.tastyhouse.domain.payment.vo.Amount;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.order.port.out.write.OrderLoadPort;
import com.tastyhouse.application.order.port.out.write.OrderSavePort;
import com.tastyhouse.application.order.service.OrderTransitionService;
import com.tastyhouse.application.payment.port.out.PgConfirmFailure;
import com.tastyhouse.application.payment.port.out.PgConfirmResult;
import com.tastyhouse.application.payment.port.out.TossPaymentDetail;
import com.tastyhouse.application.payment.port.out.write.PaymentLoadPort;
import com.tastyhouse.application.payment.port.out.write.PaymentSavePort;
import com.tastyhouse.application.payment.port.out.write.TossPaymentRecordSavePort;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.testsupport.shared.logging.LogCapture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentConfirmationServiceTest {

    private static final MemberId MEMBER_ID = MemberId.of(7L);
    private static final MemberId OTHER_MEMBER_ID = MemberId.of(99L);
    private static final OrderId ORDER_ID = OrderId.of(100L);
    private static final PaymentId PAYMENT_ID = PaymentId.of(200L);
    private static final ShopId SHOP_ID = ShopId.of(1L);
    private static final ShopId OTHER_SHOP_ID = ShopId.of(2L);

    private LogCapture logCapture;

    @BeforeEach
    void attachLogCapture() {
        logCapture = LogCapture.attachTo(PaymentConfirmationService.class);
    }

    @AfterEach
    void detachLogCapture() {
        logCapture.detach();
    }

    @Test
    @DisplayName("토스 반영(성공): 결제 완료·주문 확정·원장 기록·완료 이벤트가 모두 일어난다")
    void applyPgConfirmation_completesAndPublishesEvent() {
        Fixture fixture = Fixture.withPendingPayment(OrderStatus.PENDING);

        fixture.service.applyPgConfirmation(MEMBER_ID, PgProvider.TOSS, "pg-order-1", successConfirmResult());

        assertThat(fixture.paymentPersistence.lastSaved.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(fixture.orderPersistence.lastSaved.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(fixture.tossPaymentRecordSavePort.saved).hasSize(1);
        assertThat(fixture.eventPublisher.published).hasSize(1);

        PaymentCompletedEvent event = (PaymentCompletedEvent) fixture.eventPublisher.published.getFirst();
        assertThat(event.isOnSitePayment()).isFalse();
        assertThat(event.memberId()).isEqualTo(MEMBER_ID);
    }

    @Test
    @DisplayName("PG 반영(성공): 담당 PG를 결제에 기록하고, 원본 응답이 없는 PG는 토스 원장을 남기지 않는다")
    void applyPgConfirmation_recordsProviderAndSkipsMissingDetail() {
        Fixture fixture = Fixture.withPendingPayment(OrderStatus.PENDING);
        PgConfirmResult withoutDetail = new PgConfirmResult(
            true, "payment-key", "DONE", 21000, LocalDateTime.of(2026, 7, 31, 10, 0),
            "https://receipt", null, null, null, null, null, null, null
        );

        fixture.service.applyPgConfirmation(MEMBER_ID, PgProvider.KAKAO, "pg-order-1", withoutDetail);

        assertThat(fixture.paymentPersistence.lastSaved.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(fixture.paymentPersistence.lastSaved.getPgProvider()).isEqualTo(PgProvider.KAKAO);
        assertThat(fixture.tossPaymentRecordSavePort.saved).isEmpty();
    }

    @Test
    @DisplayName("토스 반영(실패): 결제를 FAILED로 저장하고 주문은 확정하지 않으며 원장은 남긴다")
    void failPgConfirmation_savesFailedWithoutConfirmingOrder() {
        Fixture fixture = Fixture.withPendingPayment(OrderStatus.PENDING);
        PgConfirmResult rejected = new PgConfirmResult(
            false, null, null, null, null, null, null, null, null, "REJECT", "한도 초과",
            PgConfirmFailure.REJECTED, detail()
        );

        fixture.service.failPgConfirmation("pg-order-1", rejected);

        assertThat(fixture.paymentPersistence.lastSaved.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(fixture.orderPersistence.lastSaved).isNull();
        assertThat(fixture.tossPaymentRecordSavePort.saved).hasSize(1);
        assertThat(fixture.eventPublisher.published).isEmpty();
    }

    @Test
    @DisplayName("토스 반영(실패): 예외를 던지지 않는다 — 실패 근거(원장·FAILED 전이)가 커밋되어야 하므로 예외 변환은 호출자 몫이다")
    void failPgConfirmation_doesNotThrow() {
        Fixture fixture = Fixture.withPendingPayment(OrderStatus.PENDING);
        PgConfirmResult rejected = new PgConfirmResult(
            false, null, null, null, null, null, null, null, null, "REJECT", "한도 초과",
            PgConfirmFailure.REJECTED, detail()
        );

        fixture.service.failPgConfirmation("pg-order-1", rejected);

        assertThat(fixture.tossPaymentRecordSavePort.saved).hasSize(1);
    }

    @Test
    @DisplayName("토스 반영(실패): 이미 처리된 결제라는 응답이면 대기 결제를 실패로 바꾸지 않고 원장만 남긴다")
    void failPgConfirmation_keepsPendingWhenAlreadyProcessed() {
        Fixture fixture = Fixture.withPendingPayment(OrderStatus.PENDING);

        fixture.service.failPgConfirmation("pg-order-1", alreadyProcessedResult());

        assertThat(fixture.paymentPersistence.lastSaved).isNull();
        assertThat(fixture.paymentPersistence.stored.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(fixture.tossPaymentRecordSavePort.saved).hasSize(1);
        assertThat(logCapture.events()).isEmpty();
    }

    @Test
    @DisplayName("토스 반영(실패): 결제가 완료된 뒤의 이미 처리됨 거절은 상태를 바꾸지 않고 로그도 남기지 않는다")
    void failPgConfirmation_keepsCompletedWhenAlreadyProcessed() {
        Fixture fixture = new Fixture(paymentIn(PaymentStatus.COMPLETED), Fixture.order(OrderStatus.CONFIRMED));

        fixture.service.failPgConfirmation("pg-order-1", alreadyProcessedResult());

        assertThat(fixture.paymentPersistence.lastSaved).isNull();
        assertThat(fixture.paymentPersistence.stored.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(logCapture.events()).isEmpty();
    }

    @Test
    @DisplayName("토스 반영(실패): 결제가 실패 상태일 때의 이미 처리됨 거절은 상태를 바꾸지 않고 원장만 남긴다")
    void failPgConfirmation_keepsFailedWhenAlreadyProcessed() {
        Fixture fixture = new Fixture(paymentIn(PaymentStatus.FAILED), Fixture.order(OrderStatus.PENDING));

        fixture.service.failPgConfirmation("pg-order-1", alreadyProcessedResult());

        assertThat(fixture.paymentPersistence.lastSaved).isNull();
        assertThat(fixture.paymentPersistence.stored.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(fixture.tossPaymentRecordSavePort.saved).hasSize(1);
        assertThat(logCapture.events()).isEmpty();
    }

    @Test
    @DisplayName("토스 반영(실패): 실패 분류가 없는 결과는 거절로 확정할 수 없으므로 대기 결제를 실패로 바꾸지 않는다")
    void failPgConfirmation_keepsPendingWhenFailureIsMissing() {
        Fixture fixture = Fixture.withPendingPayment(OrderStatus.PENDING);
        PgConfirmResult unclassified = new PgConfirmResult(
            false, null, null, null, null, null, null, null, null, "REJECT", "한도 초과",
            null, detail()
        );

        fixture.service.failPgConfirmation("pg-order-1", unclassified);

        assertThat(fixture.paymentPersistence.lastSaved).isNull();
        assertThat(fixture.paymentPersistence.stored.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(fixture.tossPaymentRecordSavePort.saved).hasSize(1);
    }

    @Test
    @DisplayName("토스 반영(실패): 먼저 커밋된 승인으로 결제가 이미 완료됐으면 실패로 덮어쓰지 않는다")
    void failPgConfirmation_doesNotOverwriteCompletedPayment() {
        Fixture fixture = new Fixture(paymentIn(PaymentStatus.COMPLETED), Fixture.order(OrderStatus.CONFIRMED));
        PgConfirmResult rejected = new PgConfirmResult(
            false, null, null, null, null, null, null, null, null, "REJECT", "한도 초과",
            PgConfirmFailure.REJECTED, detail()
        );

        fixture.service.failPgConfirmation("pg-order-1", rejected);

        assertThat(fixture.paymentPersistence.lastSaved).isNull();
        assertThat(fixture.paymentPersistence.stored.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    @DisplayName("토스 반영(성공): 중복 요청의 실패 반영이 먼저 커밋돼 결제가 실패여도 PG 승인 성공으로 완료하고 주문을 확정한다")
    void applyPgConfirmation_recoversFailedPayment() {
        Fixture fixture = new Fixture(paymentIn(PaymentStatus.FAILED), Fixture.order(OrderStatus.PENDING));

        fixture.service.applyPgConfirmation(MEMBER_ID, PgProvider.TOSS, "pg-order-1", successConfirmResult());

        assertThat(fixture.paymentPersistence.lastSaved.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(fixture.orderPersistence.lastSaved.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(fixture.eventPublisher.published).hasSize(1);
    }

    @Test
    @DisplayName("토스 반영(성공): 이미 완료된 결제의 중복 승인은 PAYMENT_NOT_PENDING_APPROVAL로 거절한다")
    void applyPgConfirmation_rejectsCompletedPayment() {
        Fixture fixture = new Fixture(paymentIn(PaymentStatus.COMPLETED), Fixture.order(OrderStatus.CONFIRMED));

        assertThatThrownBy(() -> fixture.service.applyPgConfirmation(MEMBER_ID, PgProvider.TOSS, "pg-order-1", successConfirmResult()))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(ApplicationErrorCode.PAYMENT_NOT_PENDING_APPROVAL.getDefaultMessage());

        assertThat(fixture.paymentPersistence.lastSaved).isNull();
        assertThat(fixture.orderPersistence.lastSaved).isNull();
    }

    @Test
    @DisplayName("토스 반영: 승인·실패 반영 모두 결제 행을 잠금 조회로만 읽고 잠금 없는 조회를 앞세우지 않는다")
    void applyAndFail_loadPaymentWithLock() {
        Fixture applyFixture = Fixture.withPendingPayment(OrderStatus.PENDING);
        applyFixture.service.applyPgConfirmation(MEMBER_ID, PgProvider.TOSS, "pg-order-1", successConfirmResult());

        Fixture failFixture = Fixture.withPendingPayment(OrderStatus.PENDING);
        failFixture.service.failPgConfirmation("pg-order-1", new PgConfirmResult(
            false, null, null, null, null, null, null, null, null, "REJECT", "한도 초과",
            PgConfirmFailure.REJECTED, detail()
        ));

        assertThat(applyFixture.paymentPersistence.lockedLoadCount).isEqualTo(1);
        assertThat(failFixture.paymentPersistence.lockedLoadCount).isEqualTo(1);
        assertThat(applyFixture.paymentPersistence.unlockedLoadCount).isZero();
        assertThat(failFixture.paymentPersistence.unlockedLoadCount).isZero();
    }

    @Test
    @DisplayName("토스 반영(성공): 이미 완료된 결제의 pgTid가 이번 paymentKey와 같으면 원장을 더 쌓지 않고 그 결제 식별자를 돌려준다(멱등)")
    void applyPgConfirmation_isIdempotentForSamePaymentKey() {
        Fixture fixture = new Fixture(paymentIn(PaymentStatus.COMPLETED, "payment-key"), Fixture.order(OrderStatus.CONFIRMED));

        PaymentId paymentId = fixture.service.applyPgConfirmation(MEMBER_ID, PgProvider.TOSS, "pg-order-1", successConfirmResult());

        assertThat(paymentId).isEqualTo(PAYMENT_ID);
        assertThat(fixture.paymentPersistence.lastSaved).isNull();
        assertThat(fixture.orderPersistence.lastSaved).isNull();
        assertThat(fixture.tossPaymentRecordSavePort.saved).isEmpty();
        assertThat(fixture.eventPublisher.published).isEmpty();
    }

    @Test
    @DisplayName("토스 반영(성공): 이미 완료된 결제의 pgTid가 이번 paymentKey와 다르면 PAYMENT_NOT_PENDING_APPROVAL로 거절한다")
    void applyPgConfirmation_rejectsCompletedPaymentWithDifferentPaymentKey() {
        Fixture fixture = new Fixture(paymentIn(PaymentStatus.COMPLETED, "other-key"), Fixture.order(OrderStatus.CONFIRMED));

        assertThatThrownBy(() -> fixture.service.applyPgConfirmation(MEMBER_ID, PgProvider.TOSS, "pg-order-1", successConfirmResult()))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(ApplicationErrorCode.PAYMENT_NOT_PENDING_APPROVAL.getDefaultMessage());

        assertThat(fixture.paymentPersistence.lastSaved).isNull();
    }

    @Test
    @DisplayName("토스 반영(실패): 결과를 알 수 없음(UNKNOWN)이면 결제를 대기로 두고 원장과 PG_CONFIRM_UNKNOWN 경고를 남긴다")
    void failPgConfirmation_keepsPendingWhenUnknown() {
        Fixture fixture = Fixture.withPendingPayment(OrderStatus.PENDING);

        fixture.service.failPgConfirmation("pg-order-1", unknownResult());

        assertThat(fixture.paymentPersistence.lastSaved).isNull();
        assertThat(fixture.paymentPersistence.stored.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(fixture.tossPaymentRecordSavePort.saved).hasSize(1);
        List<ILoggingEvent> events = logCapture.events();
        assertThat(events).hasSize(1);
        assertThat(events.getFirst().getLevel()).isEqualTo(Level.WARN);
        assertThat(events.getFirst().getFormattedMessage()).contains("PG_CONFIRM_UNKNOWN", "pg-order-1", "paymentId=200", "PG_SERVER_ERROR");
    }

    @Test
    @DisplayName("PG 시도 기록: 결제를 잠금 조회로 읽어 담당 PG만 남기고 저장한다")
    void recordPgAttempt_recordsProviderWithLock() {
        Fixture fixture = new Fixture(paymentWithoutProvider(), Fixture.order(OrderStatus.PENDING));

        fixture.service.recordPgAttempt("pg-order-1", PgProvider.TOSS);

        assertThat(fixture.paymentPersistence.lastSaved.getPgProvider()).isEqualTo(PgProvider.TOSS);
        assertThat(fixture.paymentPersistence.lastSaved.getPgTid()).isNull();
        assertThat(fixture.paymentPersistence.lastSaved.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(fixture.paymentPersistence.lockedLoadCount).isEqualTo(1);
        assertThat(fixture.paymentPersistence.unlockedLoadCount).isZero();
    }

    @Test
    @DisplayName("PG 시도 기록: batch 대사 일정을 횟수 0, 다음 시각 지금+10분으로 초기화한다")
    void recordPgAttempt_resetsReconcileSchedule() {
        Fixture fixture = new Fixture(paymentWithoutProvider(), Fixture.order(OrderStatus.PENDING));
        LocalDateTime before = LocalDateTime.now();

        fixture.service.recordPgAttempt("pg-order-1", PgProvider.TOSS);

        assertThat(fixture.paymentPersistence.reconcileScheduledPaymentId).isEqualTo(PAYMENT_ID);
        assertThat(fixture.paymentPersistence.reconcileScheduledAttemptCount).isZero();
        assertThat(fixture.paymentPersistence.reconcileScheduledNextAt)
            .isBetween(before.plusMinutes(10), LocalDateTime.now().plusMinutes(10));
    }

    @Test
    @DisplayName("PG 시도 기록: 잠금 조회 결과가 취소된 결제면 PAYMENT_NOT_PENDING_APPROVAL로 거절하고 저장하지 않는다")
    void recordPgAttempt_rejectsCancelledPayment() {
        Fixture fixture = new Fixture(paymentIn(PaymentStatus.CANCELLED), Fixture.order(OrderStatus.CANCELLED));

        assertThatThrownBy(() -> fixture.service.recordPgAttempt("pg-order-1", PgProvider.TOSS))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(ApplicationErrorCode.PAYMENT_NOT_PENDING_APPROVAL.getDefaultMessage());

        assertThat(fixture.paymentPersistence.lastSaved).isNull();
        assertThat(fixture.paymentPersistence.reconcileScheduledAttemptCount).isNull();
        assertThat(fixture.paymentPersistence.lockedLoadCount).isEqualTo(1);
    }

    @Test
    @DisplayName("조회 대사: 대기 결제·금액 일치·주문 확정 가능이면 결제 완료·주문 확정·완료 이벤트까지 반영한다(COMPLETED)")
    void reconcileApproved_completesPendingPayment() {
        Fixture fixture = Fixture.withPendingPayment(OrderStatus.PENDING);

        PgReconcileResult result = fixture.service.reconcileApproved("pg-order-1", PgProvider.TOSS, successConfirmResult());

        assertThat(result.outcome()).isEqualTo(PgReconcileOutcome.COMPLETED);
        assertThat(result.paymentId()).isEqualTo(PAYMENT_ID);
        assertThat(fixture.paymentPersistence.lastSaved.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(fixture.paymentPersistence.lastSaved.getPgTid()).isEqualTo("payment-key");
        assertThat(fixture.orderPersistence.lastSaved.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(fixture.tossPaymentRecordSavePort.saved).hasSize(1);
        PaymentCompletedEvent event = (PaymentCompletedEvent) fixture.eventPublisher.published.getFirst();
        assertThat(event.memberId()).isEqualTo(MEMBER_ID);
        assertThat(fixture.paymentPersistence.lockedLoadCount).isEqualTo(1);
    }

    @Test
    @DisplayName("조회 대사: 실패로 기록된 결제도 승인 확인이면 완료로 바로잡는다(COMPLETED)")
    void reconcileApproved_recoversFailedPayment() {
        Fixture fixture = new Fixture(paymentIn(PaymentStatus.FAILED), Fixture.order(OrderStatus.PENDING));

        PgReconcileResult result = fixture.service.reconcileApproved("pg-order-1", PgProvider.TOSS, successConfirmResult());

        assertThat(result.outcome()).isEqualTo(PgReconcileOutcome.COMPLETED);
        assertThat(fixture.paymentPersistence.lastSaved.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    @DisplayName("조회 대사: 이미 완료된 결제는 아무것도 바꾸지 않는다(ALREADY_COMPLETED)")
    void reconcileApproved_returnsAlreadyCompleted() {
        Fixture fixture = new Fixture(paymentIn(PaymentStatus.COMPLETED, "payment-key"), Fixture.order(OrderStatus.CONFIRMED));

        PgReconcileResult result = fixture.service.reconcileApproved("pg-order-1", PgProvider.TOSS, successConfirmResult());

        assertThat(result.outcome()).isEqualTo(PgReconcileOutcome.ALREADY_COMPLETED);
        assertThat(result.paymentId()).isEqualTo(PAYMENT_ID);
        assertThat(fixture.paymentPersistence.lastSaved).isNull();
        assertThat(fixture.orderPersistence.lastSaved).isNull();
        assertThat(fixture.tossPaymentRecordSavePort.saved).isEmpty();
        assertThat(fixture.eventPublisher.published).isEmpty();
    }

    @Test
    @DisplayName("조회 대사: PG 금액이 결제 금액과 다르면 상태도 토스 상세 기록도 남기지 않고 PG_DB_MISMATCH 에러를 남긴다(AMOUNT_MISMATCH)")
    void reconcileApproved_keepsStateOnAmountMismatch() {
        Fixture fixture = Fixture.withPendingPayment(OrderStatus.PENDING);
        PgConfirmResult differentAmount = new PgConfirmResult(
            true, "payment-key", "DONE", 9000, LocalDateTime.of(2026, 7, 31, 10, 0),
            "https://receipt", null, null, null, null, null, null, detail()
        );

        PgReconcileResult result = fixture.service.reconcileApproved("pg-order-1", PgProvider.TOSS, differentAmount);

        assertThat(result.outcome()).isEqualTo(PgReconcileOutcome.AMOUNT_MISMATCH);
        assertThat(fixture.paymentPersistence.lastSaved).isNull();
        assertThat(fixture.tossPaymentRecordSavePort.saved).isEmpty();
        assertThat(fixture.paymentPersistence.stored.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(fixture.orderPersistence.lastSaved).isNull();
        assertThat(fixture.eventPublisher.published).isEmpty();
        List<ILoggingEvent> events = logCapture.events();
        assertThat(events).hasSize(1);
        assertThat(events.getFirst().getLevel()).isEqualTo(Level.ERROR);
        assertThat(events.getFirst().getFormattedMessage()).contains("PG_DB_MISMATCH", "pg-order-1", "paymentAmount=21000", "pgAmount=9000");
    }

    @Test
    @DisplayName("조회 대사: 주문이 이미 취소돼 확정할 수 없으면 결제만 완료로 바꾸고 주문 확정·이벤트는 생략하며 PG_DB_MISMATCH 에러를 남긴다(ORDER_NOT_CONFIRMABLE)")
    void reconcileApproved_completesPaymentOnlyWhenOrderCancelled() {
        Fixture fixture = Fixture.withPendingPayment(OrderStatus.CANCELLED);

        PgReconcileResult result = fixture.service.reconcileApproved("pg-order-1", PgProvider.TOSS, successConfirmResult());

        assertThat(result.outcome()).isEqualTo(PgReconcileOutcome.ORDER_NOT_CONFIRMABLE);
        assertThat(fixture.paymentPersistence.lastSaved.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(fixture.orderPersistence.lastSaved).isNull();
        assertThat(fixture.eventPublisher.published).isEmpty();
        List<ILoggingEvent> events = logCapture.events();
        assertThat(events).hasSize(1);
        assertThat(events.getFirst().getLevel()).isEqualTo(Level.ERROR);
        assertThat(events.getFirst().getFormattedMessage()).contains("PG_DB_MISMATCH", "pg-order-1", "orderStatus=CANCELLED");
    }

    @Test
    @DisplayName("조회 대사: 취소된 결제는 PAYMENT_NOT_PENDING_APPROVAL로 거절하고 아무것도 바꾸지 않는다")
    void reconcileApproved_rejectsCancelledPayment() {
        Fixture fixture = new Fixture(paymentIn(PaymentStatus.CANCELLED), Fixture.order(OrderStatus.CANCELLED));

        assertThatThrownBy(() -> fixture.service.reconcileApproved("pg-order-1", PgProvider.TOSS, successConfirmResult()))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(ApplicationErrorCode.PAYMENT_NOT_PENDING_APPROVAL.getDefaultMessage());

        assertThat(fixture.paymentPersistence.lastSaved).isNull();
        assertThat(fixture.orderPersistence.lastSaved).isNull();
        assertThat(fixture.eventPublisher.published).isEmpty();
    }

    @Test
    @DisplayName("토스 사전 검증: 요청 금액이 결제 금액과 다르면 거절하고 아무 상태도 바꾸지 않는다(PG 호출 전)")
    void preparePgConfirmation_rejectsAmountMismatch() {
        Fixture fixture = Fixture.withPendingPayment(OrderStatus.PENDING);

        assertThatThrownBy(() -> fixture.service.preparePgConfirmation(MEMBER_ID, "pg-order-1", 999))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(ApplicationErrorCode.PAYMENT_AMOUNT_MISMATCH.getDefaultMessage());

        assertThat(fixture.paymentPersistence.lastSaved).isNull();
        assertThat(fixture.orderPersistence.lastSaved).isNull();
        assertThat(fixture.tossPaymentRecordSavePort.saved).isEmpty();
    }

    @Test
    @DisplayName("토스 사전 검증: 통과하면 PG 요청에 쓸 결제 식별자·주문번호·금액을 확정해 돌려준다")
    void preparePgConfirmation_returnsPgRequestTarget() {
        Fixture fixture = Fixture.withPendingPayment(OrderStatus.PENDING);

        PgConfirmationTarget target = fixture.service.preparePgConfirmation(MEMBER_ID, "pg-order-1", 21000);

        assertThat(target.paymentId()).isEqualTo(PAYMENT_ID.value());
        assertThat(target.pgOrderId()).isEqualTo("pg-order-1");
        assertThat(target.amount()).isEqualTo(21000);
        assertThat(fixture.paymentPersistence.lastSaved).isNull();
    }

    @Test
    @DisplayName("토스 승인: 다른 회원의 주문이면 사전 검증·반영 모두 PAYMENT_ACCESS_DENIED로 거절한다")
    void confirmPgPayment_rejectsOtherMember() {
        Fixture prepareFixture = Fixture.withPendingPayment(OrderStatus.PENDING);
        assertThatThrownBy(() -> prepareFixture.service.preparePgConfirmation(OTHER_MEMBER_ID, "pg-order-1", 21000))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(ApplicationErrorCode.PAYMENT_ACCESS_DENIED.getDefaultMessage());

        Fixture applyFixture = Fixture.withPendingPayment(OrderStatus.PENDING);
        assertThatThrownBy(() -> applyFixture.service.applyPgConfirmation(OTHER_MEMBER_ID, PgProvider.TOSS, "pg-order-1", successConfirmResult()))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(ApplicationErrorCode.PAYMENT_ACCESS_DENIED.getDefaultMessage());

        assertThat(applyFixture.paymentPersistence.lastSaved).isNull();
        assertThat(applyFixture.orderPersistence.lastSaved).isNull();
    }

    @Test
    @DisplayName("현장결제 완료: 결제를 완료하고 적립 포인트를 주문에 반영한 뒤 주문을 확정한다")
    void completeOnSitePayment_appliesEarnedPointAndConfirmsOrder() {
        Fixture fixture = Fixture.withPendingPayment(OrderStatus.PENDING, PaymentMethod.CASH_ON_SITE);

        fixture.service.completeOnSitePayment(SHOP_ID, PAYMENT_ID);

        assertThat(fixture.paymentPersistence.lockedLoadCount).isEqualTo(1);
        assertThat(fixture.paymentPersistence.unlockedLoadCount).isZero();
        assertThat(fixture.paymentPersistence.lastSaved.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(fixture.orderPersistence.lastSaved.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);

        assertThat(fixture.orderPersistence.lastSaved.getEarnedPoint()).isEqualTo(2100);

        PaymentCompletedEvent event = (PaymentCompletedEvent) fixture.eventPublisher.published.getFirst();
        assertThat(event.isOnSitePayment()).isTrue();
        assertThat(event.memberId()).isEqualTo(MEMBER_ID);
    }

    @Test
    @DisplayName("현장결제 완료: 다른 가게 주문의 결제면 없는 결제로 거절하고 아무것도 바꾸지 않는다")
    void completeOnSitePayment_rejectsOtherShopAsNotFound() {
        Fixture fixture = Fixture.withPendingPayment(OrderStatus.PENDING, PaymentMethod.CASH_ON_SITE);

        assertThatThrownBy(() -> fixture.service.completeOnSitePayment(OTHER_SHOP_ID, PAYMENT_ID))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(ApplicationErrorCode.PAYMENT_NOT_FOUND.getDefaultMessage());

        assertThat(fixture.paymentPersistence.lockedLoadCount).isEqualTo(1);
        assertThat(fixture.paymentPersistence.unlockedLoadCount).isZero();
        assertThat(fixture.paymentPersistence.lastSaved).isNull();
        assertThat(fixture.orderPersistence.lastSaved).isNull();
        assertThat(fixture.eventPublisher.published).isEmpty();
    }

    @Test
    @DisplayName("현장결제 완료: 현장결제 수단이 아니면 거절한다")
    void completeOnSitePayment_rejectsNonOnSiteMethod() {
        Fixture fixture = Fixture.withPendingPayment(OrderStatus.PENDING, PaymentMethod.CREDIT_CARD);

        assertThatThrownBy(() -> fixture.service.completeOnSitePayment(SHOP_ID, PAYMENT_ID))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(ApplicationErrorCode.PAYMENT_NOT_ON_SITE.getDefaultMessage());

        assertThat(fixture.paymentPersistence.lastSaved).isNull();
        assertThat(fixture.orderPersistence.lastSaved).isNull();
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
            .hasMessageContaining(ApplicationErrorCode.PAYMENT_INVALID_ORDER_STATUS.getDefaultMessage());
    }

    @Test
    @DisplayName("결제 개시: 이미 결제가 진행 중인 주문이면 거절한다")
    void open_rejectsDuplicatePayment() {
        Fixture fixture = Fixture.withPendingPayment(OrderStatus.PENDING);
        fixture.paymentPersistence.existsByOrderId = true;

        assertThatThrownBy(() -> fixture.service.open(MEMBER_ID, ORDER_ID, PaymentMethod.CREDIT_CARD))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(ApplicationErrorCode.PAYMENT_ALREADY_IN_PROGRESS.getDefaultMessage());
    }

    private static Payment paymentIn(PaymentStatus paymentStatus) {
        return paymentIn(paymentStatus, null);
    }

    private static Payment paymentIn(PaymentStatus paymentStatus, String pgTid) {
        return Payment.reconstitute(
            PAYMENT_ID.value(), ORDER_ID, PaymentMethod.CREDIT_CARD, paymentStatus, new Amount(21000),
            PgProvider.TOSS, pgTid, "pg-order-1", null, null, null, null, null, null, null,
            LocalDateTime.of(2026, 7, 31, 9, 0)
        );
    }

    private static Payment paymentWithoutProvider() {
        return Payment.reconstitute(
            PAYMENT_ID.value(), ORDER_ID, PaymentMethod.CREDIT_CARD, PaymentStatus.PENDING, new Amount(21000),
            null, null, "pg-order-1", null, null, null, null, null, null, null,
            LocalDateTime.of(2026, 7, 31, 9, 0)
        );
    }

    private static PgConfirmResult unknownResult() {
        return new PgConfirmResult(
            false, null, null, null, null, null, null, null, null, "PG_SERVER_ERROR", "결제 승인하기 API 호출에 실패했습니다: 500",
            PgConfirmFailure.UNKNOWN, detail()
        );
    }

    private static PgConfirmResult successConfirmResult() {
        return new PgConfirmResult(
            true, "payment-key", "DONE", 21000, LocalDateTime.of(2026, 7, 31, 10, 0),
            "https://receipt", "신한카드", "1234-****", 0, null, null, null, detail()
        );
    }

    private static PgConfirmResult alreadyProcessedResult() {
        return new PgConfirmResult(
            false, null, null, null, null, null, null, null, null, "ALREADY_PROCESSED_PAYMENT", "이미 처리된 결제 입니다.",
            PgConfirmFailure.ALREADY_PROCESSED, detail()
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

    private static final class Fixture {

        private final PaymentConfirmationService service;
        private final PaymentPersistenceStub paymentPersistence;
        private final OrderPersistenceStub orderPersistence;
        private final TossPaymentRecordSavePortStub tossPaymentRecordSavePort;
        private final DomainEventPublisherStub eventPublisher;

        private Fixture(Payment payment, Order order) {
            this.paymentPersistence = new PaymentPersistenceStub(payment);
            this.orderPersistence = new OrderPersistenceStub(order);
            this.tossPaymentRecordSavePort = new TossPaymentRecordSavePortStub();
            this.eventPublisher = new DomainEventPublisherStub();
            this.service = new PaymentConfirmationService(
                paymentPersistence,
                paymentPersistence,
                tossPaymentRecordSavePort,
                new OrderTransitionService(orderPersistence, orderPersistence),
                eventPublisher
            );
        }

        private static Fixture withPendingPayment(OrderStatus orderStatus) {
            return withPendingPayment(orderStatus, PaymentMethod.CREDIT_CARD);
        }

        private static Fixture withPendingPayment(OrderStatus orderStatus, PaymentMethod paymentMethod) {
            return new Fixture(pendingPayment(paymentMethod), order(orderStatus));
        }

        private static Payment pendingPayment(PaymentMethod paymentMethod) {
            return Payment.reconstitute(
                PAYMENT_ID.value(), ORDER_ID, paymentMethod, PaymentStatus.PENDING, new Amount(21000),
                PgProvider.TOSS, null, "pg-order-1", null, null, null, null, null, null, null,
                LocalDateTime.of(2026, 7, 31, 9, 0)
            );
        }

        private static Order order(OrderStatus orderStatus) {
            return Order.reconstitute(
                ORDER_ID.value(), MEMBER_ID, SHOP_ID, "ORD-1", null, orderStatus,
                "주문자", "01012345678", "orderer@tastyhouse.com",
                21000, 0, 0, 0, 0, 0, 0, 21000, OrderDeliveryDestination.none(), OrderSchedule.none(), null, 0, 0,
                false, LocalDateTime.of(2026, 7, 31, 9, 0), LocalDateTime.of(2026, 7, 31, 9, 0)
            );
        }
    }

    private static final class PaymentPersistenceStub implements PaymentLoadPort, PaymentSavePort {

        private final Payment stored;
        private Payment lastSaved;
        private boolean existsByOrderId;
        private int lockedLoadCount;
        private int unlockedLoadCount;
        private PaymentId reconcileScheduledPaymentId;
        private Integer reconcileScheduledAttemptCount;
        private LocalDateTime reconcileScheduledNextAt;

        private PaymentPersistenceStub(Payment stored) {
            this.stored = stored;
        }

        @Override
        public Optional<Payment> findById(PaymentId paymentId) {
            unlockedLoadCount++;
            return Optional.of(stored);
        }

        @Override
        public Optional<Payment> findByIdForUpdate(PaymentId paymentId) {
            lockedLoadCount++;
            return Optional.of(stored);
        }

        @Override
        public Optional<Payment> findByPgOrderId(String pgOrderId) {
            unlockedLoadCount++;
            return Optional.of(stored);
        }

        @Override
        public Optional<Payment> findByPgOrderIdForUpdate(String pgOrderId) {
            lockedLoadCount++;
            return Optional.of(stored);
        }

        @Override
        public boolean existsByOrderId(OrderId orderId) {
            return existsByOrderId;
        }

        @Override
        public List<Payment> findAllPgAttemptedReconcileDueByStatusCreatedBetween(
            PaymentStatus status, LocalDateTime from, LocalDateTime to, LocalDateTime dueAt, int limit
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int findReconcileAttemptCount(PaymentId paymentId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Payment save(Payment payment) {
            this.lastSaved = payment;
            return payment;
        }

        @Override
        public void saveReconcileSchedule(PaymentId paymentId, int attemptCount, LocalDateTime nextAttemptAt) {
            this.reconcileScheduledPaymentId = paymentId;
            this.reconcileScheduledAttemptCount = attemptCount;
            this.reconcileScheduledNextAt = nextAttemptAt;
        }

        @Override
        public boolean saveReconcileScheduleIfDue(
            PaymentId paymentId, int expectedAttemptCount, LocalDateTime dueAt, LocalDateTime nextAttemptAt
        ) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class OrderPersistenceStub implements OrderLoadPort, OrderSavePort {

        private final Order stored;
        private Order lastSaved;

        private OrderPersistenceStub(Order stored) {
            this.stored = stored;
        }

        @Override
        public Optional<Order> findByIdIncludingDeleted(OrderId orderId) {
            return Optional.of(stored);
        }

        @Override
        public Order save(Order order) {
            this.lastSaved = order;
            return order;
        }
    }

    private static final class TossPaymentRecordSavePortStub implements TossPaymentRecordSavePort {

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

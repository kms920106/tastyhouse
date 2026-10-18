package com.tastyhouse.application.payment.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.payment.model.Payment;
import com.tastyhouse.domain.payment.model.PaymentMethod;
import com.tastyhouse.domain.payment.model.PaymentStatus;
import com.tastyhouse.domain.payment.model.PgProvider;
import com.tastyhouse.domain.payment.vo.Amount;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.application.payment.port.out.PgConfirmFailure;
import com.tastyhouse.application.payment.port.out.PgConfirmResult;
import com.tastyhouse.application.payment.port.out.PgPaymentGatewayPort;
import com.tastyhouse.application.payment.port.out.PgPaymentLookupResult;
import com.tastyhouse.application.payment.port.out.PgPaymentLookupStatus;
import com.tastyhouse.application.payment.port.out.write.PaymentLoadPort;
import com.tastyhouse.testsupport.shared.logging.LogCapture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PaymentReconcileSchedulerServiceTest {

    private static final int AMOUNT = 21000;

    private PaymentLoadPort paymentLoadPort;
    private PgPaymentGatewayPort pgPaymentGatewayPort;
    private PaymentReconcileExecutor paymentReconcileExecutor;
    private PaymentReconcileSchedulerService service;
    private LogCapture logCapture;

    @BeforeEach
    void setUp() {
        paymentLoadPort = mock(PaymentLoadPort.class);
        pgPaymentGatewayPort = mock(PgPaymentGatewayPort.class);
        paymentReconcileExecutor = mock(PaymentReconcileExecutor.class);
        service = new PaymentReconcileSchedulerService(paymentLoadPort, pgPaymentGatewayPort, paymentReconcileExecutor);
        logCapture = LogCapture.attachTo(PaymentReconcileSchedulerService.class);
        when(paymentReconcileExecutor.claimNextAttemptInNewTx(any(PaymentId.class), any(LocalDateTime.class)))
            .thenReturn(Optional.of(new PaymentReconcileClaim(1, LocalDateTime.now().plusMinutes(9))));
    }

    @AfterEach
    void detachLogCapture() {
        logCapture.detach();
    }

    @Test
    @DisplayName("대상은 다음 대사 시각이 된 PENDING 결제를 7일 전부터 10분 전까지 최대 100건 조회한다")
    void queriesPendingPaymentsWithinWindow() {
        givenTargets(List.of());

        LocalDateTime before = LocalDateTime.now();
        service.reconcileUnconfirmedPayments();

        ArgumentCaptor<LocalDateTime> from = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> to = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> dueAt = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(paymentLoadPort).findAllPgAttemptedReconcileDueByStatusCreatedBetween(
            eq(PaymentStatus.PENDING), from.capture(), to.capture(), dueAt.capture(), eq(100)
        );
        assertThat(Duration.between(from.getValue(), to.getValue()))
            .isEqualTo(Duration.ofDays(7).minusMinutes(10));
        assertThat(to.getValue()).isCloseTo(before.minusMinutes(10), within(5, ChronoUnit.SECONDS));
        assertThat(dueAt.getValue()).isEqualTo(to.getValue().plusMinutes(10));
    }

    @Test
    @DisplayName("대상이 0건이면 PG를 조회하지 않고 INFO 이상 로그 없이(DEBUG로만) 바로 끝낸다")
    void noTargets_endsImmediately() {
        givenTargets(List.of());

        service.reconcileUnconfirmedPayments();

        verifyNoInteractions(pgPaymentGatewayPort, paymentReconcileExecutor);
        assertThat(logCapture.events()).isEmpty();
    }

    @Test
    @DisplayName("조회 결과가 APPROVED면 조회한 승인 결과로 승인 반영을 맡긴다")
    void approved_reconcilesWithLookupResult() {
        givenTargets(List.of(pendingPayment(1L, "pg-order-1")));
        PgConfirmResult approval = approvedResult();
        givenLookup("pg-order-1", PgPaymentLookupStatus.APPROVED, approval);
        when(paymentReconcileExecutor.reconcileApprovedInNewTx("pg-order-1", PgProvider.TOSS, approval))
            .thenReturn(new PgReconcileResult(PaymentId.of(1L), PgReconcileOutcome.COMPLETED));

        service.reconcileUnconfirmedPayments();

        verify(paymentReconcileExecutor).reconcileApprovedInNewTx("pg-order-1", PgProvider.TOSS, approval);
        verify(paymentReconcileExecutor, never()).failUnapprovedInNewTx(anyString(), any());
    }

    @Test
    @DisplayName("조회 결과가 NOT_APPROVED면 조회 결과(REJECTED)로 실패 반영을 맡긴다")
    void notApproved_failsWithLookupResult() {
        givenTargets(List.of(pendingPayment(1L, "pg-order-1")));
        PgConfirmResult rejected = failedResult(PgConfirmFailure.REJECTED, "NOT_FOUND_PAYMENT");
        givenLookup("pg-order-1", PgPaymentLookupStatus.NOT_APPROVED, rejected);

        service.reconcileUnconfirmedPayments();

        verify(paymentReconcileExecutor).failUnapprovedInNewTx("pg-order-1", rejected);
        verify(paymentReconcileExecutor, never()).reconcileApprovedInNewTx(anyString(), any(), any());
    }

    @Test
    @DisplayName("조회 결과가 UNKNOWN이면 대사 차례만 소비하고 결제는 건드리지 않은 채 다음 차례로 넘긴다")
    void unknown_isSkipped() {
        givenTargets(List.of(pendingPayment(1L, "pg-order-1")));
        givenLookup("pg-order-1", PgPaymentLookupStatus.UNKNOWN, failedResult(PgConfirmFailure.UNKNOWN, "PG_SERVER_ERROR"));
        givenClaim(3, LocalDateTime.now().plusMinutes(79));

        service.reconcileUnconfirmedPayments();

        verify(paymentReconcileExecutor).claimNextAttemptInNewTx(eq(PaymentId.of(1L)), any(LocalDateTime.class));
        verify(paymentReconcileExecutor, never()).reconcileApprovedInNewTx(anyString(), any(), any());
        verify(paymentReconcileExecutor, never()).failUnapprovedInNewTx(anyString(), any());
        List<ILoggingEvent> warns = eventsAt(Level.WARN);
        assertThat(warns).hasSize(1);
        assertThat(warns.getFirst().getFormattedMessage()).contains("PG_CONFIRM_UNKNOWN", "pg-order-1", "attempt=3", "PG_SERVER_ERROR");
    }

    @Test
    @DisplayName("건마다 PG 조회 전에, 대상 조회와 같은 실행 시작 시각 기준으로 대사 차례를 선점한다")
    void claimsAttemptBeforeLookup() {
        givenTargets(List.of(pendingPayment(1L, "pg-order-1")));
        givenLookup("pg-order-1", PgPaymentLookupStatus.UNKNOWN, failedResult(PgConfirmFailure.UNKNOWN, "PG_SERVER_ERROR"));

        service.reconcileUnconfirmedPayments();

        ArgumentCaptor<LocalDateTime> dueAt = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(paymentLoadPort).findAllPgAttemptedReconcileDueByStatusCreatedBetween(
            eq(PaymentStatus.PENDING), any(LocalDateTime.class), any(LocalDateTime.class), dueAt.capture(), anyInt()
        );
        InOrder inOrder = inOrder(paymentReconcileExecutor, pgPaymentGatewayPort);
        ArgumentCaptor<LocalDateTime> now = ArgumentCaptor.forClass(LocalDateTime.class);
        inOrder.verify(paymentReconcileExecutor).claimNextAttemptInNewTx(eq(PaymentId.of(1L)), now.capture());
        inOrder.verify(pgPaymentGatewayPort).lookupPayment("TOSS", "pg-order-1");
        assertThat(now.getValue()).isEqualTo(dueAt.getValue());
    }

    @Test
    @DisplayName("대사 차례 선점이 예외로 실패하면 ERROR를 남기고 그 결제는 PG에 묻지 않은 채 다음 결제로 넘어간다")
    void claimFailure_skipsLookupAndContinues() {
        givenTargets(List.of(pendingPayment(1L, "pg-order-1"), pendingPayment(2L, "pg-order-2")));
        when(paymentReconcileExecutor.claimNextAttemptInNewTx(eq(PaymentId.of(1L)), any(LocalDateTime.class)))
            .thenThrow(new IllegalStateException("선점 실패 모사"));
        PgConfirmResult rejected = failedResult(PgConfirmFailure.REJECTED, "NOT_FOUND_PAYMENT");
        givenLookup("pg-order-2", PgPaymentLookupStatus.NOT_APPROVED, rejected);

        service.reconcileUnconfirmedPayments();

        verify(pgPaymentGatewayPort, never()).lookupPayment("TOSS", "pg-order-1");
        verify(paymentReconcileExecutor).failUnapprovedInNewTx("pg-order-2", rejected);
        List<ILoggingEvent> errors = eventsAt(Level.ERROR);
        assertThat(errors).hasSize(1);
        assertThat(errors.getFirst().getFormattedMessage()).contains("차례 선점 실패", "pg-order-1");
        assertThat(eventsAt(Level.INFO).getFirst().getFormattedMessage()).contains("차례 선점 실패 1 건", "오류 0 건");
    }

    @Test
    @DisplayName("다른 실행이 대사 차례를 먼저 가져간 결제는 PG에 묻지도 반영하지도 않는다")
    void claimedElsewhere_isNotLookedUp() {
        givenTargets(List.of(pendingPayment(1L, "pg-order-1")));
        when(paymentReconcileExecutor.claimNextAttemptInNewTx(eq(PaymentId.of(1L)), any(LocalDateTime.class)))
            .thenReturn(Optional.empty());

        service.reconcileUnconfirmedPayments();

        verifyNoInteractions(pgPaymentGatewayPort);
        verify(paymentReconcileExecutor, never()).reconcileApprovedInNewTx(anyString(), any(), any());
        verify(paymentReconcileExecutor, never()).failUnapprovedInNewTx(anyString(), any());
        assertThat(eventsAt(Level.INFO).getFirst().getFormattedMessage()).contains("다른 실행이 처리 1 건");
    }

    @Test
    @DisplayName("대사 차례 선점이 3건 연속 실패하면(DB 장애 추정) 남은 결제를 건드리지 않고 이번 주기를 중단한다")
    void consecutiveClaimFailures_abortRun() {
        givenTargets(List.of(
            pendingPayment(1L, "pg-order-1"),
            pendingPayment(2L, "pg-order-2"),
            pendingPayment(3L, "pg-order-3"),
            pendingPayment(4L, "pg-order-4")
        ));
        when(paymentReconcileExecutor.claimNextAttemptInNewTx(any(PaymentId.class), any(LocalDateTime.class)))
            .thenThrow(new IllegalStateException("DB 장애 모사"));

        service.reconcileUnconfirmedPayments();

        verify(paymentReconcileExecutor, times(3)).claimNextAttemptInNewTx(any(PaymentId.class), any(LocalDateTime.class));
        verify(paymentReconcileExecutor, never()).claimNextAttemptInNewTx(eq(PaymentId.of(4L)), any(LocalDateTime.class));
        verifyNoInteractions(pgPaymentGatewayPort);
        assertThat(eventsAt(Level.ERROR).getLast().getFormattedMessage()).contains("3 건 연속 실패", "남은 대상 1 건");
        assertThat(eventsAt(Level.INFO).getFirst().getFormattedMessage()).contains("차례 선점 실패 3 건");
    }

    @Test
    @DisplayName("선점이 한 번 성공하면 연속 실패 횟수가 초기화되어, 띄엄띄엄 실패해서는 주기가 중단되지 않는다")
    void nonConsecutiveClaimFailures_doNotAbort() {
        givenTargets(List.of(
            pendingPayment(1L, "pg-order-1"),
            pendingPayment(2L, "pg-order-2"),
            pendingPayment(3L, "pg-order-3"),
            pendingPayment(4L, "pg-order-4")
        ));
        IllegalStateException failure = new IllegalStateException("선점 실패 모사");
        when(paymentReconcileExecutor.claimNextAttemptInNewTx(eq(PaymentId.of(1L)), any(LocalDateTime.class))).thenThrow(failure);
        when(paymentReconcileExecutor.claimNextAttemptInNewTx(eq(PaymentId.of(2L)), any(LocalDateTime.class))).thenThrow(failure);
        when(paymentReconcileExecutor.claimNextAttemptInNewTx(eq(PaymentId.of(4L)), any(LocalDateTime.class))).thenThrow(failure);
        givenLookup("pg-order-3", PgPaymentLookupStatus.NOT_APPROVED, failedResult(PgConfirmFailure.REJECTED, "NOT_FOUND_PAYMENT"));

        service.reconcileUnconfirmedPayments();

        verify(paymentReconcileExecutor).claimNextAttemptInNewTx(eq(PaymentId.of(4L)), any(LocalDateTime.class));
        assertThat(eventsAt(Level.ERROR)).extracting(ILoggingEvent::getFormattedMessage).noneMatch(message -> message.contains("연속 실패"));
    }

    @Test
    @DisplayName("7일 대사 창의 마지막 시도에서도 확정하지 못하면 PG_RECONCILE_MANUAL_REQUIRED 에러로 수동 대사가 필요함을 알린다")
    void lastAttemptUnresolved_logsManualRequired() {
        LocalDateTime createdAt = LocalDateTime.now().minusDays(7).plusMinutes(15);
        givenTargets(List.of(pendingPayment(1L, "pg-order-1", createdAt)));
        givenClaim(14, LocalDateTime.now().plusHours(24).minusMinutes(1));
        givenLookup("pg-order-1", PgPaymentLookupStatus.UNKNOWN, failedResult(PgConfirmFailure.UNKNOWN, "PG_SERVER_ERROR"));

        service.reconcileUnconfirmedPayments();

        List<ILoggingEvent> errors = eventsAt(Level.ERROR);
        assertThat(errors).hasSize(1);
        assertThat(errors.getFirst().getFormattedMessage())
            .contains("PG_RECONCILE_MANUAL_REQUIRED", "pg-order-1", "attempt=14");
        assertThat(eventsAt(Level.INFO).getFirst().getFormattedMessage()).contains("수동 대사 필요 1 건");
    }

    @Test
    @DisplayName("마지막 시도라도 금액 불일치로 PENDING에 남으면 수동 대사 필요 에러를 남긴다")
    void lastAttemptAmountMismatch_logsManualRequired() {
        LocalDateTime createdAt = LocalDateTime.now().minusDays(7).plusMinutes(15);
        givenTargets(List.of(pendingPayment(1L, "pg-order-1", createdAt)));
        givenClaim(14, LocalDateTime.now().plusHours(24).minusMinutes(1));
        PgConfirmResult approval = approvedResult();
        givenLookup("pg-order-1", PgPaymentLookupStatus.APPROVED, approval);
        when(paymentReconcileExecutor.reconcileApprovedInNewTx("pg-order-1", PgProvider.TOSS, approval))
            .thenReturn(new PgReconcileResult(PaymentId.of(1L), PgReconcileOutcome.AMOUNT_MISMATCH));

        service.reconcileUnconfirmedPayments();

        assertThat(eventsAt(Level.ERROR)).extracting(ILoggingEvent::getFormattedMessage)
            .anyMatch(message -> message.contains("PG_RECONCILE_MANUAL_REQUIRED"));
    }

    @Test
    @DisplayName("마지막 시도에서 승인이 확인돼 결제가 완료되면(주문 확정 불가 포함) 수동 대사 필요 에러를 남기지 않는다")
    void lastAttemptOrderNotConfirmable_doesNotLogManualRequired() {
        LocalDateTime createdAt = LocalDateTime.now().minusDays(7).plusMinutes(15);
        givenTargets(List.of(pendingPayment(1L, "pg-order-1", createdAt)));
        givenClaim(14, LocalDateTime.now().plusHours(24).minusMinutes(1));
        PgConfirmResult approval = approvedResult();
        givenLookup("pg-order-1", PgPaymentLookupStatus.APPROVED, approval);
        when(paymentReconcileExecutor.reconcileApprovedInNewTx("pg-order-1", PgProvider.TOSS, approval))
            .thenReturn(new PgReconcileResult(PaymentId.of(1L), PgReconcileOutcome.ORDER_NOT_CONFIRMABLE));

        service.reconcileUnconfirmedPayments();

        assertThat(eventsAt(Level.ERROR)).extracting(ILoggingEvent::getFormattedMessage)
            .noneMatch(message -> message.contains("PG_RECONCILE_MANUAL_REQUIRED"));
    }

    @Test
    @DisplayName("마지막 시도에서 확정되면(미승인 실패 처리) 수동 대사 필요 에러를 남기지 않는다")
    void lastAttemptResolved_doesNotLogManualRequired() {
        LocalDateTime createdAt = LocalDateTime.now().minusDays(7).plusMinutes(15);
        givenTargets(List.of(pendingPayment(1L, "pg-order-1", createdAt)));
        givenClaim(14, LocalDateTime.now().plusHours(24).minusMinutes(1));
        givenLookup("pg-order-1", PgPaymentLookupStatus.NOT_APPROVED, failedResult(PgConfirmFailure.REJECTED, "NOT_FOUND_PAYMENT"));

        service.reconcileUnconfirmedPayments();

        assertThat(eventsAt(Level.ERROR)).isEmpty();
    }

    @Test
    @DisplayName("다음 시도가 아직 7일 창 안에 들어오면 확정하지 못해도 수동 대사 필요 에러를 남기지 않는다")
    void attemptWithinWindow_doesNotLogManualRequired() {
        LocalDateTime createdAt = LocalDateTime.now().minusDays(5);
        givenTargets(List.of(pendingPayment(1L, "pg-order-1", createdAt)));
        givenClaim(12, LocalDateTime.now().plusHours(24).minusMinutes(1));
        givenLookup("pg-order-1", PgPaymentLookupStatus.UNKNOWN, failedResult(PgConfirmFailure.UNKNOWN, "PG_SERVER_ERROR"));

        service.reconcileUnconfirmedPayments();

        assertThat(eventsAt(Level.ERROR)).isEmpty();
    }

    @Test
    @DisplayName("APPROVED 조회 뒤 DB 반영이 예외로 실패하면 PG_DB_MISMATCH 에러를 남긴다")
    void approvedReflectionFailure_logsDbMismatch() {
        givenTargets(List.of(pendingPayment(1L, "pg-order-1")));
        PgConfirmResult approval = approvedResult();
        givenLookup("pg-order-1", PgPaymentLookupStatus.APPROVED, approval);
        when(paymentReconcileExecutor.reconcileApprovedInNewTx("pg-order-1", PgProvider.TOSS, approval))
            .thenThrow(new IllegalStateException("반영 실패 모사"));

        service.reconcileUnconfirmedPayments();

        List<ILoggingEvent> errors = eventsAt(Level.ERROR);
        assertThat(errors).hasSize(1);
        assertThat(errors.getFirst().getFormattedMessage()).contains("PG_DB_MISMATCH", "pg-order-1", "payment-key");
    }

    @Test
    @DisplayName("한 건의 조회나 반영이 실패해도 예외를 삼키고 나머지 건을 계속 처리한다")
    void failureOfOnePayment_isIsolated() {
        givenTargets(List.of(
            pendingPayment(1L, "pg-order-1"),
            pendingPayment(2L, "pg-order-2"),
            pendingPayment(3L, "pg-order-3")
        ));
        when(pgPaymentGatewayPort.lookupPayment("TOSS", "pg-order-1")).thenThrow(new IllegalStateException("조회 실패 모사"));
        PgConfirmResult approval = approvedResult();
        givenLookup("pg-order-2", PgPaymentLookupStatus.APPROVED, approval);
        when(paymentReconcileExecutor.reconcileApprovedInNewTx("pg-order-2", PgProvider.TOSS, approval))
            .thenThrow(new IllegalStateException("반영 실패 모사"));
        PgConfirmResult rejected = failedResult(PgConfirmFailure.REJECTED, "NOT_FOUND_PAYMENT");
        givenLookup("pg-order-3", PgPaymentLookupStatus.NOT_APPROVED, rejected);

        assertThatCode(service::reconcileUnconfirmedPayments).doesNotThrowAnyException();

        verify(paymentReconcileExecutor).reconcileApprovedInNewTx("pg-order-2", PgProvider.TOSS, approval);
        verify(paymentReconcileExecutor).failUnapprovedInNewTx("pg-order-3", rejected);
    }

    private List<ILoggingEvent> eventsAt(Level level) {
        return logCapture.events().stream().filter(event -> event.getLevel() == level).toList();
    }

    private void givenTargets(List<Payment> payments) {
        when(paymentLoadPort.findAllPgAttemptedReconcileDueByStatusCreatedBetween(
            eq(PaymentStatus.PENDING), any(LocalDateTime.class), any(LocalDateTime.class), any(LocalDateTime.class), anyInt()
        )).thenReturn(payments);
    }

    private void givenClaim(int attemptCount, LocalDateTime nextAttemptAt) {
        when(paymentReconcileExecutor.claimNextAttemptInNewTx(eq(PaymentId.of(1L)), any(LocalDateTime.class)))
            .thenReturn(Optional.of(new PaymentReconcileClaim(attemptCount, nextAttemptAt)));
    }

    private void givenLookup(String pgOrderId, PgPaymentLookupStatus status, PgConfirmResult result) {
        when(pgPaymentGatewayPort.lookupPayment("TOSS", pgOrderId)).thenReturn(new PgPaymentLookupResult(status, result));
    }

    private static Payment pendingPayment(Long id, String pgOrderId) {
        return pendingPayment(id, pgOrderId, LocalDateTime.now().minusDays(1));
    }

    private static Payment pendingPayment(Long id, String pgOrderId, LocalDateTime createdAt) {
        return Payment.reconstitute(
            id, OrderId.of(id + 100), PaymentMethod.CREDIT_CARD, PaymentStatus.PENDING, new Amount(AMOUNT),
            PgProvider.TOSS, null, pgOrderId, null, null, null, null, null, null, null,
            createdAt
        );
    }

    private static PgConfirmResult approvedResult() {
        return new PgConfirmResult(true, "payment-key", "DONE", AMOUNT, null, null, null, null, null, null, null, null, null);
    }

    private static PgConfirmResult failedResult(PgConfirmFailure failure, String errorCode) {
        return new PgConfirmResult(false, null, null, null, null, null, null, null, null, errorCode, "실패", failure, null);
    }
}

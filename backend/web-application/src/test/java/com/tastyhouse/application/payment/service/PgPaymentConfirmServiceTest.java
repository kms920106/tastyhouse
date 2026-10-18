package com.tastyhouse.application.payment.service;

import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.payment.model.PgProvider;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.application.payment.port.in.PgPaymentConfirmCommand;
import com.tastyhouse.application.payment.port.out.PgConfirmFailure;
import com.tastyhouse.application.payment.port.out.PgConfirmResult;
import com.tastyhouse.application.payment.port.out.PgPaymentGatewayPort;
import com.tastyhouse.application.payment.port.out.PgPaymentLookupResult;
import com.tastyhouse.application.payment.port.out.PgPaymentLookupStatus;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.WebErrorCode;
import com.tastyhouse.testsupport.shared.logging.LogCapture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PgPaymentConfirmServiceTest {

    private static final Long MEMBER_ID = 7L;
    private static final MemberId MEMBER_ID_VO = MemberId.of(MEMBER_ID);
    private static final PaymentId PAYMENT_ID = PaymentId.of(200L);
    private static final String PG_ORDER_ID = "pg-order-1";
    private static final int AMOUNT = 21000;

    private PaymentConfirmationExecutor executor;
    private PgPaymentGatewayPort gateway;
    private PgPaymentConfirmService service;
    private LogCapture logCapture;

    @BeforeEach
    void setUp() {
        executor = mock(PaymentConfirmationExecutor.class);
        gateway = mock(PgPaymentGatewayPort.class);
        service = new PgPaymentConfirmService(executor, gateway);
        when(executor.prepareInNewTx(MEMBER_ID_VO, PG_ORDER_ID, AMOUNT))
            .thenReturn(new PgConfirmationTarget(PAYMENT_ID.value(), PG_ORDER_ID, AMOUNT));
        when(gateway.supports("TOSS")).thenReturn(true);
        logCapture = LogCapture.attachTo(PgPaymentConfirmService.class);
    }

    @AfterEach
    void detachLogCapture() {
        logCapture.detach();
    }

    @Test
    @DisplayName("성공: 사전 확인 → PG 시도 기록 → 승인 호출 → 승인 반영 순서로 진행하고 결제 식별자를 돌려준다")
    void success_recordsAttemptBeforeConfirmAndApplies() {
        PgConfirmResult approved = approved();
        givenConfirmReturns(approved);
        when(executor.applyInNewTx(MEMBER_ID_VO, PgProvider.TOSS, PG_ORDER_ID, approved)).thenReturn(PAYMENT_ID);

        Long paymentId = service.confirmPgPayment(command());

        assertThat(paymentId).isEqualTo(PAYMENT_ID.value());
        InOrder order = inOrder(executor, gateway);
        order.verify(executor).prepareInNewTx(MEMBER_ID_VO, PG_ORDER_ID, AMOUNT);
        order.verify(executor).recordPgAttemptInNewTx(PG_ORDER_ID, PgProvider.TOSS);
        order.verify(gateway).confirmPayment("TOSS", PAYMENT_ID.value(), "payment-key", PG_ORDER_ID, AMOUNT);
        order.verify(executor).applyInNewTx(MEMBER_ID_VO, PgProvider.TOSS, PG_ORDER_ID, approved);
        verify(gateway, never()).lookupPayment(anyString(), anyString());
    }

    @Test
    @DisplayName("미지원 PG사면 PG 시도를 기록하지 않고 PG도 호출하지 않은 채 PG_PROVIDER_UNSUPPORTED로 거절한다")
    void unsupportedProvider_rejectsWithoutRecordingAttempt() {
        when(gateway.supports("TOSS")).thenReturn(false);

        assertThatThrownBy(() -> service.confirmPgPayment(command()))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ApplicationErrorCode.PG_PROVIDER_UNSUPPORTED));

        verify(executor, never()).recordPgAttemptInNewTx(anyString(), any());
        verify(gateway, never()).confirmPayment(anyString(), any(), anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("PG 시도 기록이 거절되면(대기 상태가 아님) PG 승인을 호출하지 않고 그 예외를 그대로 던진다")
    void recordAttemptRejected_doesNotCallPg() {
        ApplicationException notPending = new ApplicationException(ApplicationErrorCode.PAYMENT_NOT_PENDING_APPROVAL);
        doThrow(notPending).when(executor).recordPgAttemptInNewTx(PG_ORDER_ID, PgProvider.TOSS);

        assertThatThrownBy(() -> service.confirmPgPayment(command())).isSameAs(notPending);

        verify(gateway, never()).confirmPayment(anyString(), any(), anyString(), anyString(), anyInt());
        verify(executor, never()).failInNewTx(anyString(), any());
    }

    @Test
    @DisplayName("REJECTED: 조회 없이 원래 결과로 실패 반영하고 400 PAYMENT_APPROVAL_FAILED(토스 원문 메시지)로 거절한다")
    void rejected_failsWithoutLookup() {
        PgConfirmResult rejected = failed(PgConfirmFailure.REJECTED, "REJECT_CARD_COMPANY", "카드사 거절");
        givenConfirmReturns(rejected);

        assertApprovalFailed("카드사 거절");

        verify(executor).failInNewTx(PG_ORDER_ID, rejected);
        verify(gateway, never()).lookupPayment(anyString(), anyString());
    }

    @Test
    @DisplayName("UNKNOWN + 조회 APPROVED: 조회 결과로 대사해 결제 식별자를 돌려주고 실패 반영은 하지 않는다")
    void unknown_lookupApproved_reconciles() {
        givenConfirmReturns(failed(PgConfirmFailure.UNKNOWN, "PG_SERVER_ERROR", "서버 오류"));
        PgConfirmResult lookedUp = approved();
        givenLookupReturns(PgPaymentLookupStatus.APPROVED, lookedUp);
        givenReconcileReturns(lookedUp, PgReconcileOutcome.COMPLETED);

        Long paymentId = service.confirmPgPayment(command());

        assertThat(paymentId).isEqualTo(PAYMENT_ID.value());
        verify(executor).reconcileApprovedInNewTx(PG_ORDER_ID, PgProvider.TOSS, lookedUp);
        verify(executor, never()).failInNewTx(anyString(), any());
        verify(executor, never()).applyInNewTx(any(), any(), anyString(), any());
    }

    @Test
    @DisplayName("UNKNOWN + 조회 NOT_APPROVED: 조회 결과로 실패 반영하고 내부 오류 문구 대신 PAYMENT_APPROVAL_FAILED 기본 메시지로 400을 돌려준다")
    void unknown_lookupNotApproved_failsWithDefaultMessage() {
        givenConfirmReturns(failed(PgConfirmFailure.UNKNOWN, "PG_SERVER_ERROR", "서버 오류"));
        PgConfirmResult lookedUp = failed(PgConfirmFailure.REJECTED, "NOT_FOUND_PAYMENT", "존재하지 않는 결제 정보 입니다.");
        givenLookupReturns(PgPaymentLookupStatus.NOT_APPROVED, lookedUp);

        assertApprovalFailed(WebErrorCode.PAYMENT_APPROVAL_FAILED.getDefaultMessage());

        verify(executor).failInNewTx(PG_ORDER_ID, lookedUp);
    }

    @Test
    @DisplayName("UNKNOWN + 조회 UNKNOWN: 조회 결과로 실패 반영(대기 유지)하고 503 PAYMENT_APPROVAL_UNCONFIRMED로 응답한다")
    void unknown_lookupUnknown_respondsUnconfirmed() {
        givenConfirmReturns(failed(PgConfirmFailure.UNKNOWN, "SYSTEM_ERROR", "타임아웃"));
        PgConfirmResult lookedUp = failed(PgConfirmFailure.UNKNOWN, "PG_SERVER_ERROR", "조회 실패");
        givenLookupReturns(PgPaymentLookupStatus.UNKNOWN, lookedUp);

        assertUnconfirmed();

        verify(executor).failInNewTx(PG_ORDER_ID, lookedUp);
    }

    @Test
    @DisplayName("ALREADY_PROCESSED + 조회 APPROVED: 이미 완료된 결제면 기존 결제 식별자를 성공으로 돌려준다(멱등 200)")
    void alreadyProcessed_lookupApproved_returnsExistingPayment() {
        givenConfirmReturns(failed(PgConfirmFailure.ALREADY_PROCESSED, "ALREADY_PROCESSED_PAYMENT", "이미 처리된 결제 입니다."));
        PgConfirmResult lookedUp = approved();
        givenLookupReturns(PgPaymentLookupStatus.APPROVED, lookedUp);
        givenReconcileReturns(lookedUp, PgReconcileOutcome.ALREADY_COMPLETED);

        Long paymentId = service.confirmPgPayment(command());

        assertThat(paymentId).isEqualTo(PAYMENT_ID.value());
        verify(executor, never()).failInNewTx(anyString(), any());
    }

    @Test
    @DisplayName("ALREADY_PROCESSED + 조회 NOT_APPROVED: 원래 결과가 아니라 조회 결과로 실패 반영하고 원래 메시지로 400을 돌려준다")
    void alreadyProcessed_lookupNotApproved_failsWithLookupResult() {
        PgConfirmResult original = failed(PgConfirmFailure.ALREADY_PROCESSED, "ALREADY_PROCESSED_PAYMENT", "이미 처리된 결제 입니다.");
        givenConfirmReturns(original);
        PgConfirmResult lookedUp = failed(PgConfirmFailure.REJECTED, "ABORTED", "토스 결제 상태: ABORTED");
        givenLookupReturns(PgPaymentLookupStatus.NOT_APPROVED, lookedUp);

        assertApprovalFailed("이미 처리된 결제 입니다.");

        verify(executor).failInNewTx(PG_ORDER_ID, lookedUp);
        verify(executor, never()).failInNewTx(PG_ORDER_ID, original);
    }

    @Test
    @DisplayName("ALREADY_PROCESSED + 조회 UNKNOWN: 조회 결과(UNKNOWN)로 실패 반영해 PG_DB_MISMATCH 없이 503으로 응답한다")
    void alreadyProcessed_lookupUnknown_respondsUnconfirmed() {
        PgConfirmResult original = failed(PgConfirmFailure.ALREADY_PROCESSED, "ALREADY_PROCESSED_PAYMENT", "이미 처리된 결제 입니다.");
        givenConfirmReturns(original);
        PgConfirmResult lookedUp = failed(PgConfirmFailure.UNKNOWN, "IN_PROGRESS", "토스 결제 상태: IN_PROGRESS");
        givenLookupReturns(PgPaymentLookupStatus.UNKNOWN, lookedUp);

        assertUnconfirmed();

        verify(executor).failInNewTx(PG_ORDER_ID, lookedUp);
        verify(executor, never()).failInNewTx(PG_ORDER_ID, original);
        assertThat(logCapture.events()).noneMatch(event -> event.getFormattedMessage().contains("PG_DB_MISMATCH"));
    }

    @Test
    @DisplayName("대사 결과 AMOUNT_MISMATCH는 503 PAYMENT_APPROVAL_UNCONFIRMED로 응답한다")
    void reconcileAmountMismatch_respondsUnconfirmed() {
        assertReconcileOutcomeRespondsUnconfirmed(PgReconcileOutcome.AMOUNT_MISMATCH);
    }

    @Test
    @DisplayName("대사 결과 ORDER_NOT_CONFIRMABLE은 503 PAYMENT_APPROVAL_UNCONFIRMED로 응답한다")
    void reconcileOrderNotConfirmable_respondsUnconfirmed() {
        assertReconcileOutcomeRespondsUnconfirmed(PgReconcileOutcome.ORDER_NOT_CONFIRMABLE);
    }

    @Test
    @DisplayName("대사 반영이 예외로 실패하면 PG_DB_MISMATCH 에러를 남기고 예외를 그대로 던진다")
    void reconcileFailure_logsMismatchAndRethrows() {
        givenConfirmReturns(failed(PgConfirmFailure.UNKNOWN, "PG_SERVER_ERROR", "서버 오류"));
        PgConfirmResult lookedUp = approved();
        givenLookupReturns(PgPaymentLookupStatus.APPROVED, lookedUp);
        IllegalStateException failure = new IllegalStateException("커밋 실패");
        when(executor.reconcileApprovedInNewTx(PG_ORDER_ID, PgProvider.TOSS, lookedUp)).thenThrow(failure);

        assertThatThrownBy(() -> service.confirmPgPayment(command())).isSameAs(failure);

        List<ILoggingEvent> errors = logCapture.events().stream()
            .filter(event -> event.getLevel() == Level.ERROR)
            .toList();
        assertThat(errors).hasSize(1);
        assertThat(errors.getFirst().getFormattedMessage()).contains("PG_DB_MISMATCH", PG_ORDER_ID, "payment-key");
    }

    private void assertReconcileOutcomeRespondsUnconfirmed(PgReconcileOutcome outcome) {
        givenConfirmReturns(failed(PgConfirmFailure.UNKNOWN, "PG_SERVER_ERROR", "서버 오류"));
        PgConfirmResult lookedUp = approved();
        givenLookupReturns(PgPaymentLookupStatus.APPROVED, lookedUp);
        givenReconcileReturns(lookedUp, outcome);

        assertUnconfirmed();

        verify(executor, never()).failInNewTx(anyString(), any());
    }

    private void givenConfirmReturns(PgConfirmResult result) {
        when(gateway.confirmPayment("TOSS", PAYMENT_ID.value(), "payment-key", PG_ORDER_ID, AMOUNT)).thenReturn(result);
    }

    private void givenLookupReturns(PgPaymentLookupStatus status, PgConfirmResult result) {
        when(gateway.lookupPayment("TOSS", PG_ORDER_ID)).thenReturn(new PgPaymentLookupResult(status, result));
    }

    private void givenReconcileReturns(PgConfirmResult approval, PgReconcileOutcome outcome) {
        when(executor.reconcileApprovedInNewTx(PG_ORDER_ID, PgProvider.TOSS, approval))
            .thenReturn(new PgReconcileResult(PAYMENT_ID, outcome));
    }

    private void assertApprovalFailed(String message) {
        assertThatThrownBy(() -> service.confirmPgPayment(command()))
            .isInstanceOf(BusinessException.class)
            .hasMessage(message)
            .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(WebErrorCode.PAYMENT_APPROVAL_FAILED));
    }

    private void assertUnconfirmed() {
        assertThatThrownBy(() -> service.confirmPgPayment(command()))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(WebErrorCode.PAYMENT_APPROVAL_UNCONFIRMED));
    }

    private static PgPaymentConfirmCommand command() {
        return new PgPaymentConfirmCommand(MEMBER_ID, "TOSS", "payment-key", PG_ORDER_ID, AMOUNT);
    }

    private static PgConfirmResult approved() {
        return new PgConfirmResult(true, "payment-key", "DONE", AMOUNT, null, null, null, null, null, null, null, null, null);
    }

    private static PgConfirmResult failed(PgConfirmFailure failure, String errorCode, String errorMessage) {
        return new PgConfirmResult(false, null, null, null, null, null, null, null, null, errorCode, errorMessage, failure, null);
    }
}

package com.tastyhouse.application.payment.service;

import ch.qos.logback.classic.Level;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.payment.model.PaymentCancelCode;
import com.tastyhouse.domain.payment.model.PaymentCancellationTarget;
import com.tastyhouse.domain.payment.model.PgProvider;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.application.payment.port.in.PaymentCancelCommand;
import com.tastyhouse.application.payment.port.out.PaymentCancelResult;
import com.tastyhouse.application.payment.port.out.PgCancelFailure;
import com.tastyhouse.application.payment.port.out.PgCancelLookupResult;
import com.tastyhouse.application.payment.port.out.PgCancelLookupStatus;
import com.tastyhouse.application.payment.port.out.PgCancelResult;
import com.tastyhouse.application.payment.port.out.PgPaymentGatewayPort;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.WebErrorCode;
import com.tastyhouse.testsupport.shared.logging.LogCapture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentCancelServiceTest {

    private static final Long MEMBER_ID = 7L;
    private static final Long PAYMENT_ID = 200L;
    private static final String PG_TID = "tid-1";
    private static final String PG_ORDER_ID = "pg-order-1";

    private PaymentCancellationExecutor cancellationExecutor;
    private PgPaymentGatewayPort pgPaymentGateway;
    private PaymentCancelService service;
    private LogCapture logCapture;

    @BeforeEach
    void setUp() {
        cancellationExecutor = mock(PaymentCancellationExecutor.class);
        pgPaymentGateway = mock(PgPaymentGatewayPort.class);
        service = new PaymentCancelService(cancellationExecutor, pgPaymentGateway);
        when(cancellationExecutor.applyInNewTx(any(MemberId.class), any(PaymentId.class), anyString(), anyBoolean()))
            .thenReturn(PaymentCancelCode.SUCCESS);
        logCapture = LogCapture.attachTo(PaymentCancelService.class);
    }

    @AfterEach
    void detachLogCapture() {
        logCapture.detach();
    }

    @Test
    @DisplayName("취소: 담당 게이트웨이가 있는 PG의 완료 결제는 그 PG로 취소를 요청한 뒤 DB를 취소한다")
    void cancelPayment_callsPgWhenProviderSupported() {
        givenTossTarget();
        givenPgCancel(new PgCancelResult(true, null, null, null));

        PaymentCancelResult result = service.cancelPayment(command());

        assertThat(result.cancelCode()).isEqualTo(PaymentCancelCode.SUCCESS.name());
        verify(pgPaymentGateway).cancelPayment("TOSS", PG_TID, "고객 변심");
        verify(cancellationExecutor).applyInNewTx(any(MemberId.class), any(PaymentId.class), anyString(), eq(true));
        verify(pgPaymentGateway, never()).lookupCancellation(any(), any(), any());
    }

    @Test
    @DisplayName("취소: 담당 게이트웨이가 없는 PG의 완료 결제는 PG를 부르지 않고 DB만 취소해 SUCCESS를 돌려준다")
    void cancelPayment_skipsPgWhenProviderUnsupported() {
        givenTarget(PaymentCancellationTarget.cancellable(true, PgProvider.KAKAO, "tid-9", PG_ORDER_ID));
        when(pgPaymentGateway.supports("KAKAO")).thenReturn(false);

        PaymentCancelResult result = service.cancelPayment(command());

        assertThat(result.cancelCode()).isEqualTo(PaymentCancelCode.SUCCESS.name());
        verify(pgPaymentGateway, never()).cancelPayment(any(), any(), any());
        verify(cancellationExecutor).applyInNewTx(any(MemberId.class), any(PaymentId.class), anyString(), eq(true));
    }

    @Test
    @DisplayName("취소: PG가 확정 거절(REJECTED)하면 조회하지 않고 DB를 바꾸지 않은 채 CANCEL_FAILED를 돌려준다")
    void cancelPayment_returnsCancelFailedWhenPgRejects() {
        givenTossTarget();
        givenPgCancel(failure("NOT_CANCELABLE_PAYMENT", PgCancelFailure.REJECTED));

        PaymentCancelResult result = service.cancelPayment(command());

        assertThat(result.cancelCode()).isEqualTo(PaymentCancelCode.CANCEL_FAILED.name());
        verify(pgPaymentGateway, never()).lookupCancellation(any(), any(), any());
        verifyNotApplied();
    }

    @Test
    @DisplayName("취소: PG 취소 요청이 예외로 끝나면 조회하지 않고 CANCEL_FAILED를 돌려준다")
    void cancelPayment_returnsCancelFailedWhenPgCallThrows() {
        givenTossTarget();
        when(pgPaymentGateway.supports("TOSS")).thenReturn(true);
        when(pgPaymentGateway.cancelPayment("TOSS", PG_TID, "고객 변심")).thenThrow(new IllegalStateException("boom"));

        PaymentCancelResult result = service.cancelPayment(command());

        assertThat(result.cancelCode()).isEqualTo(PaymentCancelCode.CANCEL_FAILED.name());
        verify(pgPaymentGateway, never()).lookupCancellation(any(), any(), any());
        verifyNotApplied();
    }

    @Test
    @DisplayName("이미 취소됨: PG 조회가 CANCELED면 기존 반영 경로(applyInNewTx, PG 취소 필요=true)로 DB를 취소하고 SUCCESS를 돌려준다")
    void cancelPayment_appliesWhenAlreadyCanceledAndLookupConfirms() {
        givenTossTarget();
        givenPgCancel(failure("ALREADY_CANCELED_PAYMENT", PgCancelFailure.ALREADY_CANCELED));
        givenLookup(PgCancelLookupStatus.CANCELED, "CANCELED");

        PaymentCancelResult result = service.cancelPayment(command());

        assertThat(result.cancelCode()).isEqualTo(PaymentCancelCode.SUCCESS.name());
        verify(pgPaymentGateway).lookupCancellation("TOSS", PG_ORDER_ID, PG_TID);
        verify(cancellationExecutor).applyInNewTx(any(MemberId.class), any(PaymentId.class), anyString(), eq(true));
    }

    @Test
    @DisplayName("결과 불명: PG 조회가 CANCELED면 DB를 취소하고 SUCCESS를 돌려준다")
    void cancelPayment_appliesWhenUnknownAndLookupConfirms() {
        givenTossTarget();
        givenPgCancel(failure("SYSTEM_ERROR", PgCancelFailure.UNKNOWN));
        givenLookup(PgCancelLookupStatus.CANCELED, "CANCELED");

        PaymentCancelResult result = service.cancelPayment(command());

        assertThat(result.cancelCode()).isEqualTo(PaymentCancelCode.SUCCESS.name());
        verify(cancellationExecutor).applyInNewTx(any(MemberId.class), any(PaymentId.class), anyString(), eq(true));
    }

    @Test
    @DisplayName("이미 취소됨: PG 조회가 NOT_CANCELED면 DB를 바꾸지 않고 PG_CANCEL_UNKNOWN 에러 로그를 남긴 뒤 CANCEL_FAILED를 돌려준다")
    void cancelPayment_failsWhenAlreadyCanceledButLookupNotCanceled() {
        givenTossTarget();
        givenPgCancel(failure("ALREADY_CANCELED_PAYMENT", PgCancelFailure.ALREADY_CANCELED));
        givenLookup(PgCancelLookupStatus.NOT_CANCELED, "DONE");

        PaymentCancelResult result = service.cancelPayment(command());

        assertThat(result.cancelCode()).isEqualTo(PaymentCancelCode.CANCEL_FAILED.name());
        verifyNotApplied();
        assertUnknownMarkerLogged();
    }

    @Test
    @DisplayName("결과 불명: PG 조회도 UNKNOWN이면 DB를 바꾸지 않고 PG_CANCEL_UNKNOWN 에러 로그를 남긴 뒤 CANCEL_FAILED를 돌려준다")
    void cancelPayment_failsWhenLookupUnknown() {
        givenTossTarget();
        givenPgCancel(failure("PG_SERVER_ERROR", PgCancelFailure.UNKNOWN));
        givenLookup(PgCancelLookupStatus.UNKNOWN, "PG_SERVER_ERROR");

        PaymentCancelResult result = service.cancelPayment(command());

        assertThat(result.cancelCode()).isEqualTo(PaymentCancelCode.CANCEL_FAILED.name());
        verifyNotApplied();
        assertUnknownMarkerLogged();
    }

    @Test
    @DisplayName("결과 불명: PG 조회가 예외로 끝나면 DB를 바꾸지 않고 PG_CANCEL_UNKNOWN 에러 로그를 남긴 뒤 CANCEL_FAILED를 돌려준다")
    void cancelPayment_failsWhenLookupThrows() {
        givenTossTarget();
        givenPgCancel(failure("SYSTEM_ERROR", PgCancelFailure.UNKNOWN));
        when(pgPaymentGateway.lookupCancellation("TOSS", PG_ORDER_ID, PG_TID)).thenThrow(new IllegalStateException("boom"));

        PaymentCancelResult result = service.cancelPayment(command());

        assertThat(result.cancelCode()).isEqualTo(PaymentCancelCode.CANCEL_FAILED.name());
        verifyNotApplied();
        assertUnknownMarkerLogged();
    }

    @Test
    @DisplayName("결제 없음: PG가 NOT_FOUND면 조회하지 않고 PG_CANCEL_NOT_FOUND 에러 로그를 남긴 뒤 CANCEL_FAILED를 돌려준다")
    void cancelPayment_logsNotFoundMarker() {
        givenTossTarget();
        givenPgCancel(failure("NOT_FOUND_PAYMENT", PgCancelFailure.NOT_FOUND));

        PaymentCancelResult result = service.cancelPayment(command());

        assertThat(result.cancelCode()).isEqualTo(PaymentCancelCode.CANCEL_FAILED.name());
        verify(pgPaymentGateway, never()).lookupCancellation(any(), any(), any());
        verifyNotApplied();
        assertThat(logCapture.events()).anyMatch(event -> event.getFormattedMessage()
            .contains("PG_CANCEL_NOT_FOUND") && event.getFormattedMessage().contains(PG_ORDER_ID));
    }

    @Test
    @DisplayName("주문번호 없음: 이미 취소됨이어도 조회할 키가 없으면 조회하지 않고 PG_CANCEL_UNKNOWN 에러 로그를 남긴 뒤 CANCEL_FAILED를 돌려준다")
    void cancelPayment_failsWithoutPgOrderId() {
        givenTarget(PaymentCancellationTarget.cancellable(true, PgProvider.TOSS, PG_TID, null));
        givenPgCancel(failure("ALREADY_CANCELED_PAYMENT", PgCancelFailure.ALREADY_CANCELED));

        PaymentCancelResult result = service.cancelPayment(command());

        assertThat(result.cancelCode()).isEqualTo(PaymentCancelCode.CANCEL_FAILED.name());
        verify(pgPaymentGateway, never()).lookupCancellation(any(), any(), any());
        verifyNotApplied();
        assertUnknownMarkerLogged();
    }

    @Test
    @DisplayName("조회 확인 후 재판정이 ALREADY_CANCELLED이고 결제도 취소 상태면 PG_DB_MISMATCH를 남기지 않는다")
    void cancelPayment_noMismatchWhenRecoveredAndPaymentCancelled() {
        givenTossTarget();
        givenPgCancel(failure("ALREADY_CANCELED_PAYMENT", PgCancelFailure.ALREADY_CANCELED));
        givenLookup(PgCancelLookupStatus.CANCELED, "CANCELED");
        givenApplyReturns(PaymentCancelCode.ALREADY_CANCELLED);
        when(cancellationExecutor.isPaymentCancelledInNewTx(any(PaymentId.class))).thenReturn(true);

        PaymentCancelResult result = service.cancelPayment(command());

        assertThat(result.cancelCode()).isEqualTo(PaymentCancelCode.ALREADY_CANCELLED.name());
        assertNoMismatch();
        assertThat(logCapture.events()).noneMatch(event -> event.getLevel() == Level.ERROR);
    }

    @Test
    @DisplayName("조회 확인 후 DB 반영이 예외로 실패하면 PG_DB_MISMATCH를 남기고 예외를 그대로 던진다")
    void cancelPayment_logsMismatchWhenRecoveredApplyThrows() {
        givenTossTarget();
        givenPgCancel(failure("ALREADY_CANCELED_PAYMENT", PgCancelFailure.ALREADY_CANCELED));
        givenLookup(PgCancelLookupStatus.CANCELED, "CANCELED");
        IllegalStateException failure = new IllegalStateException("db down");
        when(cancellationExecutor.applyInNewTx(any(MemberId.class), any(PaymentId.class), anyString(), anyBoolean()))
            .thenThrow(failure);

        assertThatThrownBy(() -> service.cancelPayment(command())).isSameAs(failure);

        assertThat(logCapture.events()).anyMatch(event -> event.getFormattedMessage()
            .contains("PG_DB_MISMATCH") && event.getFormattedMessage().contains("PG 조회로 취소 확인"));
    }

    @Test
    @DisplayName("실패 결과에 분류(failure)가 없으면 조회하지 않고 CANCEL_FAILED를 돌려준다")
    void cancelPayment_failsWhenFailureMissing() {
        givenTossTarget();
        givenPgCancel(new PgCancelResult(false, "X", "토스 메시지", null));

        PaymentCancelResult result = service.cancelPayment(command());

        assertThat(result.cancelCode()).isEqualTo(PaymentCancelCode.CANCEL_FAILED.name());
        verify(pgPaymentGateway, never()).lookupCancellation(any(), any(), any());
        verifyNotApplied();
    }

    @Test
    @DisplayName("조회 확인 후 재판정이 ALREADY_PREPARING이면 'PG 조회로 취소 확인' 문구의 PG_DB_MISMATCH를 남긴다")
    void cancelPayment_logsMismatchWhenRecoveredAndOrderPreparing() {
        givenTossTarget();
        givenPgCancel(failure("ALREADY_CANCELED_PAYMENT", PgCancelFailure.ALREADY_CANCELED));
        givenLookup(PgCancelLookupStatus.CANCELED, "CANCELED");
        givenApplyReturns(PaymentCancelCode.ALREADY_PREPARING);

        PaymentCancelResult result = service.cancelPayment(command());

        assertThat(result.cancelCode()).isEqualTo(PaymentCancelCode.ALREADY_PREPARING.name());
        assertThat(logCapture.events()).anyMatch(event -> event.getFormattedMessage()
            .contains("PG_DB_MISMATCH") && event.getFormattedMessage().contains("PG 조회로 취소 확인"));
    }

    @Test
    @DisplayName("이미 취소됨이 조회로 정상 복구되면 ERROR 로그를 남기지 않는다")
    void cancelPayment_noErrorLogWhenRecovered() {
        givenTossTarget();
        givenPgCancel(failure("ALREADY_CANCELED_PAYMENT", PgCancelFailure.ALREADY_CANCELED));
        givenLookup(PgCancelLookupStatus.CANCELED, "CANCELED");

        service.cancelPayment(command());

        assertThat(logCapture.events()).noneMatch(event -> event.getLevel() == Level.ERROR);
    }

    @Test
    @DisplayName("PG 취소 성공 후 재판정이 ALREADY_CANCELLED이고 결제도 취소 상태면 PG_DB_MISMATCH를 남기지 않는다")
    void cancelPayment_noMismatchWhenPgCanceledAndPaymentCancelled() {
        givenTossTarget();
        givenPgCancel(new PgCancelResult(true, null, null, null));
        givenApplyReturns(PaymentCancelCode.ALREADY_CANCELLED);
        when(cancellationExecutor.isPaymentCancelledInNewTx(any(PaymentId.class))).thenReturn(true);

        PaymentCancelResult result = service.cancelPayment(command());

        assertThat(result.cancelCode()).isEqualTo(PaymentCancelCode.ALREADY_CANCELLED.name());
        assertNoMismatch();
        assertThat(logCapture.events()).noneMatch(event -> event.getLevel() == Level.ERROR);
    }

    @Test
    @DisplayName("PG 취소 성공 후 재판정이 ALREADY_PREPARING이면 PG_DB_MISMATCH를 남긴다")
    void cancelPayment_logsMismatchWhenPgCanceledAndOrderPreparing() {
        givenTossTarget();
        givenPgCancel(new PgCancelResult(true, null, null, null));
        givenApplyReturns(PaymentCancelCode.ALREADY_PREPARING);

        PaymentCancelResult result = service.cancelPayment(command());

        assertThat(result.cancelCode()).isEqualTo(PaymentCancelCode.ALREADY_PREPARING.name());
        assertMismatch();
        verify(cancellationExecutor, never()).isPaymentCancelledInNewTx(any(PaymentId.class));
    }

    @Test
    @DisplayName("PG 취소 성공 후 재판정이 ALREADY_CANCELLED여도 결제가 완료 상태로 남아 있으면(주문만 취소) PG_DB_MISMATCH를 남긴다")
    void cancelPayment_logsMismatchWhenOnlyOrderCancelled() {
        givenTossTarget();
        givenPgCancel(new PgCancelResult(true, null, null, null));
        givenApplyReturns(PaymentCancelCode.ALREADY_CANCELLED);
        when(cancellationExecutor.isPaymentCancelledInNewTx(any(PaymentId.class))).thenReturn(false);

        PaymentCancelResult result = service.cancelPayment(command());

        assertThat(result.cancelCode()).isEqualTo(PaymentCancelCode.ALREADY_CANCELLED.name());
        assertMismatch();
    }

    @Test
    @DisplayName("조회 확인 후 재판정이 ALREADY_CANCELLED여도 결제가 완료 상태로 남아 있으면(주문만 취소) 'PG 조회로 취소 확인' 문구의 PG_DB_MISMATCH를 남긴다")
    void cancelPayment_logsMismatchWhenRecoveredAndOnlyOrderCancelled() {
        givenTossTarget();
        givenPgCancel(failure("ALREADY_CANCELED_PAYMENT", PgCancelFailure.ALREADY_CANCELED));
        givenLookup(PgCancelLookupStatus.CANCELED, "CANCELED");
        givenApplyReturns(PaymentCancelCode.ALREADY_CANCELLED);
        when(cancellationExecutor.isPaymentCancelledInNewTx(any(PaymentId.class))).thenReturn(false);

        PaymentCancelResult result = service.cancelPayment(command());

        assertThat(result.cancelCode()).isEqualTo(PaymentCancelCode.ALREADY_CANCELLED.name());
        assertThat(logCapture.events()).anyMatch(event -> event.getFormattedMessage()
            .contains("PG_DB_MISMATCH") && event.getFormattedMessage().contains("PG 조회로 취소 확인"));
    }

    @Test
    @DisplayName("PG 취소 성공 후 재판정이 ALREADY_CANCELLED인데 결제 상태 확인이 예외로 끝나면 PG_DB_MISMATCH를 남긴다")
    void cancelPayment_logsMismatchWhenPaymentStatusCheckThrows() {
        givenTossTarget();
        givenPgCancel(new PgCancelResult(true, null, null, null));
        givenApplyReturns(PaymentCancelCode.ALREADY_CANCELLED);
        when(cancellationExecutor.isPaymentCancelledInNewTx(any(PaymentId.class)))
            .thenThrow(new IllegalStateException("db down"));

        PaymentCancelResult result = service.cancelPayment(command());

        assertThat(result.cancelCode()).isEqualTo(PaymentCancelCode.ALREADY_CANCELLED.name());
        assertMismatch();
    }

    @Test
    @DisplayName("취소: 현장결제·미승인 결제는 PG 지원 여부를 묻지도 않고 DB만 취소한다")
    void cancelPayment_skipsPgWhenNotRequired() {
        givenTarget(PaymentCancellationTarget.cancellable(false, null, null, null));

        PaymentCancelResult result = service.cancelPayment(command());

        assertThat(result.cancelCode()).isEqualTo(PaymentCancelCode.SUCCESS.name());
        verify(pgPaymentGateway, never()).supports(any());
        verify(pgPaymentGateway, never()).cancelPayment(any(), any(), any());
        verify(cancellationExecutor).applyInNewTx(any(MemberId.class), any(PaymentId.class), anyString(), eq(false));
    }

    @Test
    @DisplayName("취소: 사전 판정이 대기 결제였는데 반영 단계에서 승인 완료가 드러나 409가 나면 PG 취소를 부르지 않고 예외를 그대로 전한다")
    void cancelPayment_propagatesConflictWhenApprovedDuringCancel() {
        givenTarget(PaymentCancellationTarget.cancellable(false, PgProvider.TOSS, null, PG_ORDER_ID));
        ApplicationException conflict = new ApplicationException(WebErrorCode.PAYMENT_CANCEL_UNCONFIRMED);
        when(cancellationExecutor.applyInNewTx(any(MemberId.class), any(PaymentId.class), anyString(), eq(false)))
            .thenThrow(conflict);

        assertThatThrownBy(() -> service.cancelPayment(command())).isSameAs(conflict);

        verify(pgPaymentGateway, never()).cancelPayment(any(), any(), any());
    }

    private void givenTossTarget() {
        givenTarget(PaymentCancellationTarget.cancellable(true, PgProvider.TOSS, PG_TID, PG_ORDER_ID));
    }

    private void givenTarget(PaymentCancellationTarget target) {
        when(cancellationExecutor.prepareInNewTx(any(MemberId.class), any(PaymentId.class))).thenReturn(target);
    }

    private void givenPgCancel(PgCancelResult cancelResult) {
        when(pgPaymentGateway.supports("TOSS")).thenReturn(true);
        when(pgPaymentGateway.cancelPayment("TOSS", PG_TID, "고객 변심")).thenReturn(cancelResult);
    }

    private void givenLookup(PgCancelLookupStatus status, String pgStatus) {
        when(pgPaymentGateway.lookupCancellation("TOSS", PG_ORDER_ID, PG_TID))
            .thenReturn(new PgCancelLookupResult(status, pgStatus));
    }

    private void givenApplyReturns(PaymentCancelCode cancelCode) {
        when(cancellationExecutor.applyInNewTx(any(MemberId.class), any(PaymentId.class), anyString(), anyBoolean()))
            .thenReturn(cancelCode);
    }

    private void assertUnknownMarkerLogged() {
        assertThat(logCapture.events()).anyMatch(event -> event.getLevel() == Level.ERROR
            && event.getFormattedMessage().contains("PG_CANCEL_UNKNOWN")
            && event.getFormattedMessage().contains(PG_TID));
    }

    private void verifyNotApplied() {
        verify(cancellationExecutor, never()).applyInNewTx(any(MemberId.class), any(PaymentId.class), anyString(), anyBoolean());
    }

    private void assertMismatch() {
        assertThat(logCapture.events()).anyMatch(event -> event.getFormattedMessage().contains("PG_DB_MISMATCH"));
    }

    private void assertNoMismatch() {
        assertThat(logCapture.events()).noneMatch(event -> event.getFormattedMessage().contains("PG_DB_MISMATCH"));
    }

    private static PgCancelResult failure(String errorCode, PgCancelFailure failure) {
        return new PgCancelResult(false, errorCode, "토스 메시지", failure);
    }

    private static PaymentCancelCommand command() {
        return new PaymentCancelCommand(MEMBER_ID, PAYMENT_ID, "고객 변심");
    }
}

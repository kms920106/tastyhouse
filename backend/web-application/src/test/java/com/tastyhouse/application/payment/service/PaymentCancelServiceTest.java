package com.tastyhouse.application.payment.service;

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
import com.tastyhouse.application.payment.port.out.PgCancelResult;
import com.tastyhouse.application.payment.port.out.PgPaymentGatewayPort;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.WebErrorCode;

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

    private PaymentCancellationExecutor cancellationExecutor;
    private PgPaymentGatewayPort pgPaymentGateway;
    private PaymentCancelService service;

    @BeforeEach
    void setUp() {
        cancellationExecutor = mock(PaymentCancellationExecutor.class);
        pgPaymentGateway = mock(PgPaymentGatewayPort.class);
        service = new PaymentCancelService(cancellationExecutor, pgPaymentGateway);
        when(cancellationExecutor.applyInNewTx(any(MemberId.class), any(PaymentId.class), anyString(), anyBoolean()))
            .thenReturn(PaymentCancelCode.SUCCESS);
    }

    @Test
    @DisplayName("취소: 담당 게이트웨이가 있는 PG의 완료 결제는 그 PG로 취소를 요청한 뒤 DB를 취소한다")
    void cancelPayment_callsPgWhenProviderSupported() {
        givenTarget(PaymentCancellationTarget.cancellable(true, PgProvider.TOSS, "tid-1"));
        when(pgPaymentGateway.supports("TOSS")).thenReturn(true);
        when(pgPaymentGateway.cancelPayment("TOSS", "tid-1", "고객 변심"))
            .thenReturn(new PgCancelResult(true, null, null));

        PaymentCancelResult result = service.cancelPayment(command());

        assertThat(result.cancelCode()).isEqualTo(PaymentCancelCode.SUCCESS.name());
        verify(pgPaymentGateway).cancelPayment("TOSS", "tid-1", "고객 변심");
        verify(cancellationExecutor).applyInNewTx(any(MemberId.class), any(PaymentId.class), anyString(), eq(true));
    }

    @Test
    @DisplayName("취소: 담당 게이트웨이가 없는 PG의 완료 결제는 PG를 부르지 않고 DB만 취소해 SUCCESS를 돌려준다")
    void cancelPayment_skipsPgWhenProviderUnsupported() {
        givenTarget(PaymentCancellationTarget.cancellable(true, PgProvider.KAKAO, "tid-9"));
        when(pgPaymentGateway.supports("KAKAO")).thenReturn(false);

        PaymentCancelResult result = service.cancelPayment(command());

        assertThat(result.cancelCode()).isEqualTo(PaymentCancelCode.SUCCESS.name());
        verify(pgPaymentGateway, never()).cancelPayment(any(), any(), any());
        verify(cancellationExecutor).applyInNewTx(any(MemberId.class), any(PaymentId.class), anyString(), eq(true));
    }

    @Test
    @DisplayName("취소: PG가 취소를 거절하면 DB를 바꾸지 않고 CANCEL_FAILED를 돌려준다")
    void cancelPayment_returnsCancelFailedWhenPgRejects() {
        givenTarget(PaymentCancellationTarget.cancellable(true, PgProvider.TOSS, "tid-1"));
        when(pgPaymentGateway.supports("TOSS")).thenReturn(true);
        when(pgPaymentGateway.cancelPayment("TOSS", "tid-1", "고객 변심"))
            .thenReturn(new PgCancelResult(false, "REJECT", "취소 불가"));

        PaymentCancelResult result = service.cancelPayment(command());

        assertThat(result.cancelCode()).isEqualTo(PaymentCancelCode.CANCEL_FAILED.name());
        verify(cancellationExecutor, never()).applyInNewTx(any(MemberId.class), any(PaymentId.class), anyString(), anyBoolean());
    }

    @Test
    @DisplayName("취소: 현장결제·미승인 결제는 PG 지원 여부를 묻지도 않고 DB만 취소한다")
    void cancelPayment_skipsPgWhenNotRequired() {
        givenTarget(PaymentCancellationTarget.cancellable(false, null, null));

        PaymentCancelResult result = service.cancelPayment(command());

        assertThat(result.cancelCode()).isEqualTo(PaymentCancelCode.SUCCESS.name());
        verify(pgPaymentGateway, never()).supports(any());
        verify(pgPaymentGateway, never()).cancelPayment(any(), any(), any());
        verify(cancellationExecutor).applyInNewTx(any(MemberId.class), any(PaymentId.class), anyString(), eq(false));
    }

    @Test
    @DisplayName("취소: 사전 판정이 대기 결제였는데 반영 단계에서 승인 완료가 드러나 409가 나면 PG 취소를 부르지 않고 예외를 그대로 전한다")
    void cancelPayment_propagatesConflictWhenApprovedDuringCancel() {
        givenTarget(PaymentCancellationTarget.cancellable(false, PgProvider.TOSS, null));
        ApplicationException conflict = new ApplicationException(WebErrorCode.PAYMENT_CANCEL_UNCONFIRMED);
        when(cancellationExecutor.applyInNewTx(any(MemberId.class), any(PaymentId.class), anyString(), eq(false)))
            .thenThrow(conflict);

        assertThatThrownBy(() -> service.cancelPayment(command())).isSameAs(conflict);

        verify(pgPaymentGateway, never()).cancelPayment(any(), any(), any());
    }

    private void givenTarget(PaymentCancellationTarget target) {
        when(cancellationExecutor.prepareInNewTx(any(MemberId.class), any(PaymentId.class))).thenReturn(target);
    }

    private static PaymentCancelCommand command() {
        return new PaymentCancelCommand(MEMBER_ID, PAYMENT_ID, "고객 변심");
    }
}

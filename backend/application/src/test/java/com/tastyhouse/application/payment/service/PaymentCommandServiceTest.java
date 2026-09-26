package com.tastyhouse.application.payment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.payment.model.PaymentCancelCode;
import com.tastyhouse.domain.payment.model.PgProvider;
import com.tastyhouse.domain.payment.port.PgPaymentGateway;
import com.tastyhouse.domain.payment.port.dto.PgCancelResult;
import com.tastyhouse.domain.payment.service.PaymentCancellationService;
import com.tastyhouse.domain.payment.service.PaymentCancellationTarget;
import com.tastyhouse.domain.payment.service.PaymentConfirmationService;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.application.payment.port.in.PaymentCancelCommand;
import com.tastyhouse.application.payment.port.out.PaymentCancelResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentCommandServiceTest {

    private static final Long MEMBER_ID = 7L;
    private static final Long PAYMENT_ID = 200L;

    private PaymentCancellationExecutor cancellationExecutor;
    private PgPaymentGateway pgPaymentGateway;
    private PaymentCommandService service;

    @BeforeEach
    void setUp() {
        cancellationExecutor = mock(PaymentCancellationExecutor.class);
        pgPaymentGateway = mock(PgPaymentGateway.class);
        service = new PaymentCommandService(
            mock(PaymentConfirmationService.class),
            mock(PaymentCancellationService.class),
            mock(PaymentConfirmationExecutor.class),
            cancellationExecutor,
            pgPaymentGateway
        );
        when(cancellationExecutor.applyInNewTx(any(MemberId.class), any(PaymentId.class), anyString()))
            .thenReturn(PaymentCancelCode.SUCCESS);
    }

    @Test
    @DisplayName("취소: 담당 게이트웨이가 있는 PG의 완료 결제는 그 PG로 취소를 요청한 뒤 DB를 취소한다")
    void cancelPayment_callsPgWhenProviderSupported() {
        givenTarget(PaymentCancellationTarget.cancellable(true, PgProvider.TOSS, "tid-1"));
        when(pgPaymentGateway.supports(PgProvider.TOSS)).thenReturn(true);
        when(pgPaymentGateway.cancelPayment(PgProvider.TOSS, "tid-1", "고객 변심"))
            .thenReturn(new PgCancelResult(true, null, null));

        PaymentCancelResult result = service.cancelPayment(command());

        assertThat(result.cancelCode()).isEqualTo(PaymentCancelCode.SUCCESS.name());
        verify(pgPaymentGateway).cancelPayment(PgProvider.TOSS, "tid-1", "고객 변심");
        verify(cancellationExecutor).applyInNewTx(any(MemberId.class), any(PaymentId.class), anyString());
    }

    @Test
    @DisplayName("취소: 담당 게이트웨이가 없는 PG의 완료 결제는 PG를 부르지 않고 DB만 취소해 SUCCESS를 돌려준다")
    void cancelPayment_skipsPgWhenProviderUnsupported() {
        givenTarget(PaymentCancellationTarget.cancellable(true, PgProvider.KAKAO, "tid-9"));
        when(pgPaymentGateway.supports(PgProvider.KAKAO)).thenReturn(false);

        PaymentCancelResult result = service.cancelPayment(command());

        assertThat(result.cancelCode()).isEqualTo(PaymentCancelCode.SUCCESS.name());
        verify(pgPaymentGateway, never()).cancelPayment(any(), any(), any());
        verify(cancellationExecutor).applyInNewTx(any(MemberId.class), any(PaymentId.class), anyString());
    }

    @Test
    @DisplayName("취소: PG가 취소를 거절하면 DB를 바꾸지 않고 CANCEL_FAILED를 돌려준다")
    void cancelPayment_returnsCancelFailedWhenPgRejects() {
        givenTarget(PaymentCancellationTarget.cancellable(true, PgProvider.TOSS, "tid-1"));
        when(pgPaymentGateway.supports(PgProvider.TOSS)).thenReturn(true);
        when(pgPaymentGateway.cancelPayment(PgProvider.TOSS, "tid-1", "고객 변심"))
            .thenReturn(new PgCancelResult(false, "REJECT", "취소 불가"));

        PaymentCancelResult result = service.cancelPayment(command());

        assertThat(result.cancelCode()).isEqualTo(PaymentCancelCode.CANCEL_FAILED.name());
        verify(cancellationExecutor, never()).applyInNewTx(any(MemberId.class), any(PaymentId.class), anyString());
    }

    @Test
    @DisplayName("취소: 현장결제·미승인 결제는 PG 지원 여부를 묻지도 않고 DB만 취소한다")
    void cancelPayment_skipsPgWhenNotRequired() {
        givenTarget(PaymentCancellationTarget.cancellable(false, null, null));

        PaymentCancelResult result = service.cancelPayment(command());

        assertThat(result.cancelCode()).isEqualTo(PaymentCancelCode.SUCCESS.name());
        verify(pgPaymentGateway, never()).supports(any());
        verify(pgPaymentGateway, never()).cancelPayment(any(), any(), any());
    }

    private void givenTarget(PaymentCancellationTarget target) {
        when(cancellationExecutor.prepareInNewTx(any(MemberId.class), any(PaymentId.class))).thenReturn(target);
    }

    private static PaymentCancelCommand command() {
        return new PaymentCancelCommand(MEMBER_ID, PAYMENT_ID, "고객 변심");
    }
}

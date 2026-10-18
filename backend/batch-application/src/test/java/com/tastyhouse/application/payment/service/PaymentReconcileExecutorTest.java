package com.tastyhouse.application.payment.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.application.payment.port.out.write.PaymentLoadPort;
import com.tastyhouse.application.payment.port.out.write.PaymentSavePort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentReconcileExecutorTest {

    private static final LocalDateTime DUE_AT = LocalDateTime.of(2026, 10, 18, 9, 0);

    private PaymentLoadPort paymentLoadPort;
    private PaymentSavePort paymentSavePort;
    private PaymentReconcileExecutor executor;

    @BeforeEach
    void setUp() {
        paymentLoadPort = mock(PaymentLoadPort.class);
        paymentSavePort = mock(PaymentSavePort.class);
        executor = new PaymentReconcileExecutor(mock(PaymentConfirmationService.class), paymentLoadPort, paymentSavePort);
    }

    @Test
    @DisplayName("실행 시작 시각(dueAt)보다 한참 늦게 선점하면 다음 시각은 선점 순간+2분 기준으로 계산해 횟수+1과 함께 돌려주고, 차례 비교에는 dueAt을 쓴다")
    void claimNextAttemptInNewTx_savesBackoffAfterClaimWhenClaimedLate() {
        PaymentId paymentId = PaymentId.of(1L);
        when(paymentLoadPort.findReconcileAttemptCount(paymentId)).thenReturn(2);
        when(paymentSavePort.saveReconcileScheduleIfDue(eq(paymentId), eq(2), eq(DUE_AT), any(LocalDateTime.class))).thenReturn(true);

        LocalDateTime before = LocalDateTime.now();
        Optional<PaymentReconcileClaim> claim = executor.claimNextAttemptInNewTx(paymentId, DUE_AT);
        LocalDateTime after = LocalDateTime.now();

        ArgumentCaptor<LocalDateTime> nextAttemptAt = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(paymentSavePort).saveReconcileScheduleIfDue(eq(paymentId), eq(2), eq(DUE_AT), nextAttemptAt.capture());
        assertThat(nextAttemptAt.getValue())
            .isBetween(
                PaymentReconcileBackoffPolicy.nextAttemptAt(2, DUE_AT, before),
                PaymentReconcileBackoffPolicy.nextAttemptAt(2, DUE_AT, after)
            );
        assertThat(Duration.between(DUE_AT, nextAttemptAt.getValue())).isPositive();
        assertThat(claim).contains(new PaymentReconcileClaim(3, nextAttemptAt.getValue()));
    }

    @Test
    @DisplayName("실행 시작 직후 선점하면 다음 시각은 선점 순간이 아니라 실행 시작 시각 격자(시작 + 간격 − 1분)에 맞춘다")
    void claimNextAttemptInNewTx_alignsToRunGridWhenClaimedEarly() {
        PaymentId paymentId = PaymentId.of(1L);
        LocalDateTime dueAt = LocalDateTime.now().minusSeconds(30);
        when(paymentLoadPort.findReconcileAttemptCount(paymentId)).thenReturn(2);
        when(paymentSavePort.saveReconcileScheduleIfDue(eq(paymentId), eq(2), eq(dueAt), any(LocalDateTime.class))).thenReturn(true);

        Optional<PaymentReconcileClaim> claim = executor.claimNextAttemptInNewTx(paymentId, dueAt);

        verify(paymentSavePort).saveReconcileScheduleIfDue(paymentId, 2, dueAt, dueAt.plusMinutes(39));
        assertThat(claim).contains(new PaymentReconcileClaim(3, dueAt.plusMinutes(39)));
    }

    @Test
    @DisplayName("조건부 저장이 0건이면(다른 실행이 먼저 차례를 가져갔거나 web 승인 시도가 일정을 초기화함) 차례를 얻지 못한 것으로 빈 값을 돌려준다")
    void claimNextAttemptInNewTx_returnsEmptyWhenAlreadyClaimed() {
        PaymentId paymentId = PaymentId.of(1L);
        when(paymentLoadPort.findReconcileAttemptCount(paymentId)).thenReturn(2);
        when(paymentSavePort.saveReconcileScheduleIfDue(eq(paymentId), eq(2), eq(DUE_AT), any(LocalDateTime.class)))
            .thenReturn(false);

        assertThat(executor.claimNextAttemptInNewTx(paymentId, DUE_AT)).isEmpty();
    }
}

package com.tastyhouse.infrastructure.payment.listener;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.payment.event.PaymentCancelledEvent;
import com.tastyhouse.domain.payment.event.PaymentCompletedEvent;
import com.tastyhouse.domain.payment.event.RefundRequestedEvent;
import com.tastyhouse.domain.payment.model.PaymentMethod;
import com.tastyhouse.domain.payment.service.PaymentConfirmationService;
import com.tastyhouse.domain.payment.vo.Amount;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.domain.payment.vo.PaymentRefundId;
import com.tastyhouse.domain.point.service.PointLedgerService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link PaymentEventListener}의 현재 동작을 봉인하는 순수 단위 테스트.
 *
 * <p>이 리스너는 로그만 남기는 다른 리스너들과 달리 <b>실제 금전 효과</b>(포인트 증감)를 낸다. 따라서
 * "무엇을 기록하는가"가 아니라 "어떤 조건에서 원장 서비스를 호출/미호출하는가"를 검증한다 — 조건 분기가
 * 잘못되면 적립이 이중으로 되거나 환급이 누락되며, 이 리스너는 AFTER_COMMIT이라 실패해도 재시도 없이
 * 유실된다.
 *
 * <p>스프링 컨텍스트 없이 리스너를 직접 생성하며, {@code REQUIRES_NEW} 트랜잭션 경계와 AFTER_COMMIT
 * 발화는 프레임워크 몫이라 검증 대상이 아니다.
 */
class PaymentEventListenerTest {

    private final PointLedgerService pointLedgerService = mock(PointLedgerService.class);
    private final PaymentConfirmationService paymentConfirmationService = mock(PaymentConfirmationService.class);

    private final PaymentEventListener listener =
        new PaymentEventListener(pointLedgerService, paymentConfirmationService);

    @Nested
    @DisplayName("결제 완료")
    class PaymentCompleted {

        @Test
        @DisplayName("현장 결제면 계산된 적립액과 적립률 문구로 포인트를 적립한다")
        void earnsPointsForOnSitePayment() {
            when(paymentConfirmationService.earnRate()).thenReturn(10);
            when(paymentConfirmationService.calculateEarnedPoint(any(Amount.class))).thenReturn(1200);

            listener.onPaymentCompleted(completedEvent(true));

            verify(pointLedgerService).earnPoints(
                eq(MemberId.of(401L)),
                eq(1200),
                eq("현장 현금 결제 적립 (10%)")
            );
        }

        @Test
        @DisplayName("PG 결제면 주문 접수 시점에 이미 적립됐으므로 아무것도 하지 않는다")
        void doesNothingForPgPayment() {
            listener.onPaymentCompleted(completedEvent(false));

            verifyNoInteractions(pointLedgerService);
            verifyNoInteractions(paymentConfirmationService);
        }

        private PaymentCompletedEvent completedEvent(boolean onSitePayment) {
            return new PaymentCompletedEvent(
                PaymentId.of(402L),
                OrderId.of(403L),
                MemberId.of(401L),
                new Amount(12000),
                onSitePayment ? PaymentMethod.CASH_ON_SITE : PaymentMethod.CREDIT_CARD,
                onSitePayment,
                LocalDateTime.of(2026, 4, 12, 19, 30)
            );
        }
    }

    @Nested
    @DisplayName("결제 취소")
    class PaymentCancelled {

        @Test
        @DisplayName("사용 포인트는 환급하고 적립 포인트는 회수한다")
        void refundsUsedAndReclaimsEarned() {
            listener.onPaymentCancelled(cancelledEvent(500, 300));

            verify(pointLedgerService).refundPoints(MemberId.of(501L), 500);
            verify(pointLedgerService).reclaimEarnedPoints(MemberId.of(501L), 300);
        }

        @Test
        @DisplayName("사용 포인트가 0이면 환급하지 않는다")
        void skipsRefundWhenNoPointUsed() {
            listener.onPaymentCancelled(cancelledEvent(0, 300));

            verify(pointLedgerService, never()).refundPoints(any(MemberId.class), anyInt());
            verify(pointLedgerService).reclaimEarnedPoints(MemberId.of(501L), 300);
        }

        @Test
        @DisplayName("적립 포인트가 0이면 회수하지 않는다")
        void skipsReclaimWhenNothingEarned() {
            listener.onPaymentCancelled(cancelledEvent(500, 0));

            verify(pointLedgerService).refundPoints(MemberId.of(501L), 500);
            verify(pointLedgerService, never()).reclaimEarnedPoints(any(MemberId.class), anyInt());
        }

        private PaymentCancelledEvent cancelledEvent(int usedPoint, int earnedPoint) {
            return new PaymentCancelledEvent(
                PaymentId.of(502L),
                OrderId.of(503L),
                MemberId.of(501L),
                usedPoint,
                earnedPoint,
                "단순 변심",
                LocalDateTime.of(2026, 4, 13, 9, 15)
            );
        }
    }

    @Nested
    @DisplayName("환불 요청 접수")
    class RefundRequested {

        /**
         * 접수 시점에 포인트가 움직이면, 이후 취소가 확정될 때 {@code PaymentCancelledEvent}가 같은
         * 금액을 다시 반영해 <b>이중 정산</b>이 된다. 그래서 "아무것도 하지 않음"이 이 핸들러의 계약이며,
         * 그 계약을 여기서 봉인한다.
         */
        @Test
        @DisplayName("접수 사실만 남기고 포인트는 건드리지 않는다 — 정산은 취소 확정 시점의 몫이다")
        void doesNotTouchPoints() {
            RefundRequestedEvent event = new RefundRequestedEvent(
                PaymentRefundId.of(601L),
                PaymentId.of(602L),
                MemberId.of(603L),
                new Amount(8000),
                "메뉴 누락",
                LocalDateTime.of(2026, 4, 14, 11, 0)
            );

            listener.onRefundRequested(event);

            verifyNoInteractions(pointLedgerService);
            verifyNoInteractions(paymentConfirmationService);
        }
    }
}

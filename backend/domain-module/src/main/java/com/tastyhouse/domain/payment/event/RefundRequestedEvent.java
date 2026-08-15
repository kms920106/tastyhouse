package com.tastyhouse.domain.payment.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.payment.vo.Amount;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.domain.payment.vo.PaymentRefundId;

/**
 * 환불 요청이 접수됐음을 알리는 도메인 이벤트(승인·정산이 아니라 <b>접수</b> 시점이다).
 *
 * <p>소비처는 {@code infrastructure/payment/listener/PaymentEventListener#onRefundRequested}이며,
 * 접수 사실만 기록한다. <b>금전 정산을 여기에 붙이지 않는다</b> — 사용 포인트 환급·적립 포인트 회수는
 * 취소가 확정될 때 {@link PaymentCancelledEvent}가 수행하므로, 접수 시점에 함께 움직이면 같은 금액이
 * 두 번 반영된다. 관리자 알림·정산 연동처럼 접수 자체에 반응하는 후속 처리만 그 핸들러에 연결한다.
 */
public record RefundRequestedEvent(
    PaymentRefundId refundId,
    PaymentId paymentId,
    MemberId memberId,
    Amount refundAmount,
    String refundReason,
    LocalDateTime requestedAt
) {
}

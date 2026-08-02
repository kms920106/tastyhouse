package com.tastyhouse.domain.payment.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.payment.vo.Amount;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.domain.payment.vo.PaymentRefundId;

/**
 * 환불 요청이 접수됐음을 알리는 도메인 이벤트(승인·정산이 아니라 <b>접수</b> 시점이다).
 *
 * <p><b>현재 리스너 없음 — 의도된 발행이다.</b> P9(도메인 이벤트 정비)에서 수신자 없는 발행 7종을
 * 검토할 때, 이 이벤트는 관리자 알림·정산 연동 등 소비 수요가 실재할 가능성이 높고 이미
 * {@code PaymentCancellationServiceTest}가 발행을 계약으로 고정하고 있어 남겼다.
 *
 * <p>따라서 "리스너가 없으니 죽은 코드"로 보고 다시 제거 대상에 올리지 않는다. 소비처를 만들 때는
 * {@code infrastructure/payment/listener/PaymentEventListener}에
 * {@code @TransactionalEventListener(AFTER_COMMIT)} 핸들러를 추가한다.
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

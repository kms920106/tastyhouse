package com.tastyhouse.webapplication.payment.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 환불 요청 command.
 *
 * <p>{@code memberId}·{@code paymentId} 두 {@code Long}이 연달아 있어 위치 기반 전달 시 조용히
 * 뒤바뀐다 — {@code toCommand}는 이름 기반 접근자로 짚어 넘긴다. 환불 금액 범위 검증은 Request의
 * jakarta.validation이, 결제 금액 대조는 도메인이 담당한다.
 */
public record PaymentRefundRequestCommand(
    Long memberId,
    Long paymentId,
    Integer refundAmount,
    String refundReason
) {
    public PaymentRefundRequestCommand {
        if (memberId == null || paymentId == null || refundAmount == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

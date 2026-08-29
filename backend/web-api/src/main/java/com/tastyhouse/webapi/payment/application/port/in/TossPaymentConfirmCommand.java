package com.tastyhouse.webapi.payment.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 토스페이먼츠 결제 승인 command.
 *
 * <p>{@code paymentKey}·{@code pgOrderId} 두 {@code String}이 연달아 있어 위치 기반 전달 시 조용히
 * 뒤바뀔 수 있다 — {@code toCommand}는 이름 기반 접근자로 짚어 넘긴다.
 */
public record TossPaymentConfirmCommand(
    Long memberId,
    String paymentKey,
    String pgOrderId,
    Integer amount
) {
    public TossPaymentConfirmCommand {
        if (memberId == null || paymentKey == null || pgOrderId == null || amount == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

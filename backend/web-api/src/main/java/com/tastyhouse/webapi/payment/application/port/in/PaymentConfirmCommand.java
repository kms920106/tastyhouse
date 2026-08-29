package com.tastyhouse.webapi.payment.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * PG 콜백 결제 승인 반영 command.
 *
 * <p><b>{@code paymentId}는 경로 변수가 아니라 요청 본문 필드다</b> — 이 엔드포인트는
 * {@code POST /api/payments/v1/confirm}이라 경로에 식별자가 없다.
 *
 * <p><b>{@code cardCompany}·{@code cardNumber}·{@code receiptUrl} 세 {@code String}이 연달아 있다.</b>
 * 위치 기반으로 옮기면 컴파일은 통과하고 값만 조용히 뒤바뀌므로, {@code toCommand}는 반드시 이름 기반
 * 접근자로 각 값을 짚어 넘긴다. 필드 순서는 서비스가 조립하는 {@code PgConfirmation.of(...)}의 인자
 * 순서와 일치시켰다.
 */
public record PaymentConfirmCommand(
    Long paymentId,
    String pgProvider,
    String pgTid,
    String pgOrderId,
    String cardCompany,
    String cardNumber,
    Integer installmentMonths,
    String receiptUrl
) {
    public PaymentConfirmCommand {
        if (paymentId == null || pgProvider == null || pgTid == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

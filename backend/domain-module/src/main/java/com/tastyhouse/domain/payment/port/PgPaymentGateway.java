package com.tastyhouse.domain.payment.port;

import com.tastyhouse.domain.payment.port.dto.PgCancelResult;
import com.tastyhouse.domain.payment.port.dto.PgConfirmResult;

public interface PgPaymentGateway {

    /**
     * PG사 결제 승인 요청. 성공/실패 시 모두 내부적으로 거래 기록을 저장한다.
     */
    PgConfirmResult confirmPayment(Long paymentId, String paymentKey, String pgOrderId, int amount);

    PgCancelResult cancelPayment(String pgTid, String cancelReason);
}

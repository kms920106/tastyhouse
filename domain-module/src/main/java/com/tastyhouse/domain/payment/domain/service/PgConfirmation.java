package com.tastyhouse.domain.payment.domain.service;

import com.tastyhouse.domain.payment.domain.model.PgProvider;

/**
 * PG 콜백으로 통보된 결제 승인 정보(도메인 서비스 입력).
 *
 * <p>{@code PaymentConfirmationService#confirm}이 결제에 반영할 PG 승인 결과를 담는다. 결제 식별자는
 * 별도 파라미터({@code PaymentId})로 받으므로 여기 포함하지 않는다.
 */
public record PgConfirmation(
    PgProvider pgProvider,
    String pgTid,
    String pgOrderId,
    String cardCompany,
    String cardNumber,
    Integer installmentMonths,
    String receiptUrl
) {

    public static PgConfirmation of(
        PgProvider pgProvider,
        String pgTid,
        String pgOrderId,
        String cardCompany,
        String cardNumber,
        Integer installmentMonths,
        String receiptUrl
    ) {
        return new PgConfirmation(
            pgProvider,
            pgTid,
            pgOrderId,
            cardCompany,
            cardNumber,
            installmentMonths,
            receiptUrl
        );
    }
}

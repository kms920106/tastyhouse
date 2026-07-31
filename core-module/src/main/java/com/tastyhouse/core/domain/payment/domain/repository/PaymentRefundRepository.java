package com.tastyhouse.core.domain.payment.domain.repository;

import com.tastyhouse.core.domain.payment.domain.model.PaymentRefund;

/**
 * 결제 환불 write 포트.
 *
 * <p>환불 요청 접수(저장)만 필요하므로 {@code save} 하나만 갖는다(공통 지침 패턴 4). 환불 내역 조회는
 * infrastructure-module의 {@code infrastructure/payment/query/PaymentQueryDao}가 담당한다.
 */
public interface PaymentRefundRepository {

    PaymentRefund save(PaymentRefund paymentRefund);
}

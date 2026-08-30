package com.tastyhouse.webapplication.payment.port.in;

import com.tastyhouse.webapplication.payment.response.PaymentRefundResponse;
import com.tastyhouse.webapplication.payment.response.PaymentResponse;

/**
 * 결제 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code PaymentQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface PaymentQueryUseCase {

    PaymentResponse getPayment(Long memberId, Long id);

    PaymentResponse getPayment(Long id);

    PaymentResponse getPaymentByOrderId(Long memberId, Long orderId);

    PaymentRefundResponse getRefund(Long refundId);
}

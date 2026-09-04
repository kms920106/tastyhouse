package com.tastyhouse.webapplication.payment.port.in;

import com.tastyhouse.webapplication.payment.port.out.PaymentCancelResult;

/**
 * 회원 결제 쓰기 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code PaymentCommandService})을 알지 않는다.
 *
 * <p>{@code cancelPayment}만 응답 record를 돌려준다 — 취소 불가 사유·PG 취소 실패가 예외가 아니라
 * 코드로 돌아오는 계약이라, 식별자만으로는 결과를 표현할 수 없기 때문이다(wire 계약 유지).
 */
public interface PaymentCommandUseCase {

    Long createPayment(PaymentCreateCommand command);

    Long confirmPayment(PaymentConfirmCommand command);

    Long confirmTossPayment(TossPaymentConfirmCommand command);

    Long completeOnSitePayment(PaymentOnSiteCompleteCommand command);

    PaymentCancelResult cancelPayment(PaymentCancelCommand command);

    Long requestRefund(PaymentRefundRequestCommand command);
}

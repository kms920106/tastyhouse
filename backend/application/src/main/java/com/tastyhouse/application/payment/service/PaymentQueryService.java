package com.tastyhouse.application.payment.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.domain.payment.vo.PaymentRefundId;
import com.tastyhouse.application.payment.port.out.PaymentQueryPort;
import com.tastyhouse.application.payment.port.out.PaymentRefundResult;
import com.tastyhouse.application.payment.port.out.PaymentResult;
import com.tastyhouse.application.payment.port.in.PaymentQueryUseCase;
import com.tastyhouse.application.payment.port.out.PaymentRefundViewResult;
import com.tastyhouse.application.payment.port.out.PaymentViewResult;

/**
 * 회원 결제 조회 서비스(web-api).
 *
 * <p>infra query DAO({@link PaymentQueryPort})만 주입해 조회하고, 읽기 계약 조립(private 매퍼)을
 * 담당한다(공통 지침 패턴 2·3). write 포트는 주입하지 않는다. 금액 VO 언랩과 enum 강등이 여기서
 * 끝나야 하는 이유는 {@code PaymentViewResult} Javadoc 참고(챕터 10).
 *
 * <p>결제 조회는 회원 스코프이므로, DAO가 주문에서 함께 투영한 {@code memberId}를 요청 회원과 대조해
 * 남의 결제 열람을 막는다.
 *
 * <p>command 경로({@link PaymentCommandService})는 식별자만 돌려주므로, 커밋 이후 컨트롤러가
 * {@link #getPayment(Long, Long)}으로 재조회해 계약을 조립한다(CQRS 분리).
 */
@Service
@WebApp
@Transactional(readOnly = true)
public class PaymentQueryService implements PaymentQueryUseCase {

    private final PaymentQueryPort paymentQueryPort;

    public PaymentQueryService(PaymentQueryPort paymentQueryPort) {
        this.paymentQueryPort = paymentQueryPort;
    }

    /**
     * 결제 단건(PK) — 요청 회원의 결제가 아니면 {@code PAYMENT_ACCESS_DENIED}.
     */
    @Override
    public PaymentViewResult getPayment(Long memberId, Long id) {
        return toPaymentViewResult(
            validateOwnership(loadPayment(id), memberId, ErrorCode.PAYMENT_ACCESS_DENIED)
        );
    }

    /**
     * 결제 단건(PK) — 회원 스코프 없이 조회한다.
     *
     * <p>PG사가 서버 간 통신으로 호출하는 승인 콜백 경로 전용이다. 인증된 회원이 없어 소유권을 대조할 수
     * 없으므로 검증 없이 조회한다(기존 동작 보존 — 콜백 승인 자체도 소유권을 검증하지 않는다).
     */
    @Override
    public PaymentViewResult getPayment(Long id) {
        return toPaymentViewResult(loadPayment(id));
    }

    private PaymentResult loadPayment(Long id) {
        return paymentQueryPort.findPaymentById(PaymentId.of(id))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    /**
     * 주문별 결제 조회 — 요청 회원의 주문이 아니면 {@code ORDER_ACCESS_DENIED}.
     *
     * <p>주문 자체가 없어도 결제가 조회되지 않으므로(DAO의 inner join) 주문 미존재와 결제 미존재를 모두
     * {@code PAYMENT_NOT_FOUND}로 응답한다.
     */
    @Override
    public PaymentViewResult getPaymentByOrderId(Long memberId, Long orderId) {
        PaymentResult result = paymentQueryPort.findPaymentByOrderId(OrderId.of(orderId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PAYMENT_NOT_FOUND));
        return toPaymentViewResult(validateOwnership(result, memberId, ErrorCode.ORDER_ACCESS_DENIED));
    }

    /**
     * 환불 요청 단건(PK).
     *
     * <p>환불 요청 command 직후의 응답 조립용 재조회다 — 소유권은 요청 시점에 이미 검증되었다.
     */
    @Override
    public PaymentRefundViewResult getRefund(Long refundId) {
        PaymentRefundResult result = paymentQueryPort.findRefundById(PaymentRefundId.of(refundId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PAYMENT_REFUND_NOT_FOUND));
        return toPaymentRefundViewResult(result);
    }

    /**
     * 조회된 결제가 요청 회원의 것인지 대조한다 — 위반 시 호출 경로에 맞는 에러 코드로 실패시킨다.
     */
    private PaymentResult validateOwnership(PaymentResult result, Long memberId, ErrorCode accessDeniedCode) {
        if (!memberId.equals(result.memberId())) {
            throw new BusinessException(accessDeniedCode);
        }
        return result;
    }

    private PaymentViewResult toPaymentViewResult(PaymentResult result) {
        return new PaymentViewResult(
            result.id(),
            result.orderId(),
            result.paymentMethod() == null ? null : result.paymentMethod().name(),
            result.paymentStatus() == null ? null : result.paymentStatus().name(),
            result.amount() == null ? null : result.amount().value(),
            result.pgProvider() == null ? null : result.pgProvider().name(),
            result.pgTid(),
            result.pgOrderId(),
            result.cardCompany(),
            result.cardNumber(),
            result.installmentMonths(),
            result.approvedAt(),
            result.cancelledAt(),
            result.cancelReason(),
            result.receiptUrl(),
            result.createdAt()
        );
    }

    private PaymentRefundViewResult toPaymentRefundViewResult(PaymentRefundResult result) {
        return new PaymentRefundViewResult(
            result.id(),
            result.paymentId(),
            result.refundAmount() == null ? null : result.refundAmount().value(),
            result.refundReason(),
            result.refundStatus() == null ? null : result.refundStatus().name(),
            result.pgRefundId(),
            result.refundedAt(),
            result.createdAt()
        );
    }
}

package com.tastyhouse.webapi.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.order.domain.vo.OrderId;
import com.tastyhouse.domain.payment.domain.model.PaymentCancelCode;
import com.tastyhouse.domain.payment.domain.model.PaymentMethod;
import com.tastyhouse.domain.payment.domain.model.PgProvider;
import com.tastyhouse.domain.payment.domain.service.PaymentCancellationService;
import com.tastyhouse.domain.payment.domain.service.PaymentConfirmationService;
import com.tastyhouse.domain.payment.domain.service.PgConfirmation;
import com.tastyhouse.domain.payment.domain.vo.PaymentId;

/**
 * 회원 결제 command 서비스(web-api).
 *
 * <p>HTTP 경계에서 받은 원시 파라미터를 도메인 입력으로 승격·조립하고, 트랜잭션 경계를 선언해 도메인
 * 서비스({@link PaymentConfirmationService}/{@link PaymentCancellationService})에 위임한다(공통 지침
 * 패턴 2). 결제·주문 동기화 불변식은 도메인 서비스가 갖고, 이 서비스는 승격과 경계만 담당한다.
 *
 * <p>{@code Long → MemberId}/{@code OrderId}/{@code PaymentId} 승격과
 * {@code String → PaymentMethod}/{@code PgProvider} 승격은 기존 경계 규칙대로 여기서 한다.
 *
 * <p>반환은 결제 식별자({@code Long}) 또는 취소 결과 코드다 — 응답 조립은 커밋 이후
 * {@link PaymentQueryService}가 재조회해 담당한다(CQRS 분리).
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentCommandService {

    private final PaymentConfirmationService paymentConfirmationService;
    private final PaymentCancellationService paymentCancellationService;

    /**
     * 결제를 생성한다.
     *
     * @return 생성된 결제 식별자
     */
    public Long createPayment(Long memberId, Long orderId, String paymentMethod) {
        MemberId memberIdVo = MemberId.of(memberId);
        OrderId orderIdVo = OrderId.of(orderId);
        PaymentId paymentId = paymentConfirmationService.open(
            memberIdVo, orderIdVo, PaymentMethod.from(paymentMethod)
        );
        return paymentId.value();
    }

    /**
     * PG 콜백으로 통보된 결제 승인을 반영한다.
     *
     * @return 승인된 결제 식별자
     */
    public Long confirmPayment(
        Long id,
        String pgProvider,
        String pgTid,
        String pgOrderId,
        String cardCompany,
        String cardNumber,
        Integer installmentMonths,
        String receiptUrl
    ) {
        PaymentId paymentId = PaymentId.of(id);
        PgConfirmation confirmation = PgConfirmation.of(
            PgProvider.from(pgProvider),
            pgTid,
            pgOrderId,
            cardCompany,
            cardNumber,
            installmentMonths,
            receiptUrl
        );
        return paymentConfirmationService.confirm(paymentId, confirmation).value();
    }

    /**
     * 토스페이먼츠 결제를 승인한다.
     *
     * @return 승인된 결제 식별자
     */
    public Long confirmTossPayment(Long memberId, String paymentKey, String pgOrderId, Integer amount) {
        MemberId memberIdVo = MemberId.of(memberId);
        PaymentId paymentId = paymentConfirmationService.confirmTossPayment(
            memberIdVo, paymentKey, pgOrderId, amount
        );
        log.info("토스 결제 승인 완료 — paymentId={}, amount={}", paymentId.value(), amount);
        return paymentId.value();
    }

    /**
     * 현장결제를 완료 처리한다.
     *
     * @return 완료된 결제 식별자
     */
    public Long completeOnSitePayment(Long memberId, Long id) {
        MemberId memberIdVo = MemberId.of(memberId);
        PaymentId paymentId = PaymentId.of(id);
        return paymentConfirmationService.completeOnSitePayment(memberIdVo, paymentId).value();
    }

    /**
     * 결제를 취소한다.
     *
     * <p>취소 불가 사유·PG 취소 실패는 예외가 아니라 코드로 돌아오므로(도메인 서비스 계약), 실패 코드는
     * 운영 추적을 위해 여기서 기록한다.
     *
     * @return 취소 결과 코드
     */
    public PaymentCancelCode cancelPayment(Long memberId, Long id, String cancelReason) {
        MemberId memberIdVo = MemberId.of(memberId);
        PaymentId paymentId = PaymentId.of(id);
        PaymentCancelCode cancelCode = paymentCancellationService.cancel(memberIdVo, paymentId, cancelReason);

        if (cancelCode != PaymentCancelCode.SUCCESS) {
            log.error("결제 취소 실패 — paymentId={}, cancelCode={}", id, cancelCode);
        }
        return cancelCode;
    }

    /**
     * 환불을 요청한다.
     *
     * @return 생성된 환불 요청 식별자
     */
    public Long requestRefund(Long memberId, Long id, Integer refundAmount, String refundReason) {
        MemberId memberIdVo = MemberId.of(memberId);
        PaymentId paymentId = PaymentId.of(id);
        return paymentCancellationService
            .requestRefund(memberIdVo, paymentId, refundAmount, refundReason)
            .value();
    }
}

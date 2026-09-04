package com.tastyhouse.application.payment.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.payment.model.PaymentCancelCode;
import com.tastyhouse.domain.payment.model.PaymentMethod;
import com.tastyhouse.domain.payment.model.PgProvider;
import com.tastyhouse.domain.payment.port.PgPaymentGateway;
import com.tastyhouse.domain.payment.port.dto.PgCancelResult;
import com.tastyhouse.domain.payment.port.dto.PgConfirmResult;
import com.tastyhouse.domain.payment.service.PaymentCancellationService;
import com.tastyhouse.domain.payment.service.PaymentCancellationTarget;
import com.tastyhouse.domain.payment.service.PaymentConfirmationService;
import com.tastyhouse.domain.payment.service.PgConfirmation;
import com.tastyhouse.domain.payment.service.TossConfirmationTarget;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.application.payment.port.out.PaymentCancelResult;
import com.tastyhouse.application.payment.port.in.PaymentCancelCommand;
import com.tastyhouse.application.payment.port.in.PaymentCommandUseCase;
import com.tastyhouse.application.payment.port.in.PaymentConfirmCommand;
import com.tastyhouse.application.payment.port.in.PaymentCreateCommand;
import com.tastyhouse.application.payment.port.in.PaymentOnSiteCompleteCommand;
import com.tastyhouse.application.payment.port.in.PaymentRefundRequestCommand;
import com.tastyhouse.application.payment.port.in.TossPaymentConfirmCommand;

/**
 * 회원 결제 command 서비스(web-api).
 *
 * <p>HTTP 경계에서 받은 원시 파라미터를 도메인 입력으로 승격·조립하고, 트랜잭션 경계를 배치해 도메인
 * 서비스({@link PaymentConfirmationService}/{@link PaymentCancellationService})에 위임한다. 결제·주문
 * 동기화 불변식은 도메인 서비스가 갖고, 이 서비스는 승격과 경계 배치만 담당한다.
 *
 * <p><b>클래스 레벨 {@code @Transactional}이 없는 것은 의도다.</b> 토스 승인·결제 취소는 PG사와의 HTTP
 * 왕복을 포함하는데, 그 왕복이 DB 트랜잭션 안에 있으면 (1) 커넥션과 결제·주문 행 락을 네트워크 지연만큼
 * 점유하고, (2) PG 처리가 성공한 뒤 커밋이 실패하면 "PG는 승인/취소, DB는 미반영"이라는 보상 불가 불일치가
 * 남는다. 그래서 reservation 도메인의 3단 구조(재시도 루프 비트랜잭션 → Executor {@code @Transactional} →
 * 도메인 서비스)와 같은 형태로 재배치했다.
 *
 * <pre>
 * ① 사전 검증  : PaymentConfirmationExecutor#prepareInNewTx  (트랜잭션, readOnly)
 * ② PG 호출    : PgPaymentGateway                            (트랜잭션 없음)  ← 이 서비스가 직접
 * ③ 결과 반영  : PaymentConfirmationExecutor#applyInNewTx    (트랜잭션)
 * </pre>
 *
 * <p>PG 왕복을 포함하지 않는 나머지 명령(결제 개시·PG 콜백 반영·현장결제 완료·환불 요청)은 DB만 다루므로
 * 종전처럼 메서드 단위 {@code @Transactional} 하나로 충분하다.
 *
 * <p><b>보상 장치</b>: ③이 실패하면 PG는 이미 처리됐으므로 자동 보상이 불가능하다. 이 경우
 * {@code PG_DB_MISMATCH} 마커와 PG 거래 식별자를 포함한 {@code log.error}를 남겨 <b>수동 개입·대조 배치의
 * 진입점</b>으로 삼는다(운영에서 이 마커로 알럿을 건다). 사용자에게는 실패를 그대로 전파해 "성공했지만
 * 반영되지 않은" 상태를 성공으로 오인하게 하지 않는다.
 *
 * <p>{@code Long → MemberId}/{@code OrderId}/{@code PaymentId} 승격과
 * {@code String → PaymentMethod}/{@code PgProvider} 승격은 기존 경계 규칙대로 여기서 한다.
 *
 * <p>반환은 결제 식별자({@code Long}) 또는 취소 결과 코드다 — 응답 조립은 커밋 이후
 * {@link PaymentQueryService}가 재조회해 담당한다(CQRS 분리).
 */
@Service
@WebApp
public class PaymentCommandService implements PaymentCommandUseCase {

    private static final Logger log = LoggerFactory.getLogger(PaymentCommandService.class);

    /** PG는 처리됐는데 DB 반영이 실패한 상태를 운영에서 검색·알럿하기 위한 로그 마커. */
    private static final String PG_DB_MISMATCH = "PG_DB_MISMATCH";

    private final PaymentConfirmationService paymentConfirmationService;
    private final PaymentCancellationService paymentCancellationService;
    private final PaymentConfirmationExecutor paymentConfirmationExecutor;
    private final PaymentCancellationExecutor paymentCancellationExecutor;
    private final PgPaymentGateway pgPaymentGateway;

    public PaymentCommandService(
        PaymentConfirmationService paymentConfirmationService,
        PaymentCancellationService paymentCancellationService,
        PaymentConfirmationExecutor paymentConfirmationExecutor,
        PaymentCancellationExecutor paymentCancellationExecutor,
        PgPaymentGateway pgPaymentGateway
    ) {
        this.paymentConfirmationService = paymentConfirmationService;
        this.paymentCancellationService = paymentCancellationService;
        this.paymentConfirmationExecutor = paymentConfirmationExecutor;
        this.paymentCancellationExecutor = paymentCancellationExecutor;
        this.pgPaymentGateway = pgPaymentGateway;
    }

    /**
     * 결제를 생성한다.
     *
     * @return 생성된 결제 식별자
     */
    @Transactional
    @Override
    public Long createPayment(PaymentCreateCommand command) {
        MemberId memberIdVo = MemberId.of(command.memberId());
        OrderId orderIdVo = OrderId.of(command.orderId());
        PaymentId paymentId = paymentConfirmationService.open(
            memberIdVo, orderIdVo, PaymentMethod.from(command.paymentMethod())
        );
        return paymentId.value();
    }

    /**
     * PG 콜백으로 통보된 결제 승인을 반영한다.
     *
     * <p>PG가 이미 승인을 마치고 그 결과를 통보하는 경로라 <b>이 메서드 안에서 PG를 호출하지 않는다</b> —
     * 외부 호출이 없으므로 트랜잭션 재배치 대상이 아니고 단일 트랜잭션으로 둔다.
     *
     * @return 승인된 결제 식별자
     */
    @Transactional
    @Override
    public Long confirmPayment(PaymentConfirmCommand command) {
        PaymentId paymentId = PaymentId.of(command.paymentId());
        PgConfirmation confirmation = PgConfirmation.of(
            PgProvider.from(command.pgProvider()),
            command.pgTid(),
            command.pgOrderId(),
            command.cardCompany(),
            command.cardNumber(),
            command.installmentMonths(),
            command.receiptUrl()
        );
        return paymentConfirmationService.confirm(paymentId, confirmation).value();
    }

    /**
     * 토스페이먼츠 결제를 승인한다 — 사전 검증(tx) → PG 승인 요청(tx 밖) → 결과 반영(tx) 3단으로 수행한다.
     *
     * <p>실패 시맨틱은 트랜잭션 재배치 이전과 동일하다.
     * <ul>
     *   <li>검증 실패(미존재·소유권·상태·금액): PG를 호출하지 않고 기존과 같은 예외 코드로 실패한다.</li>
     *   <li>PG 승인 거절: PG 응답 원본을 원장에 남기고 결제를 {@code FAILED}로 전이한 뒤
     *       {@code PAYMENT_APPROVAL_FAILED}로 실패한다(메시지도 기존과 동일하게 PG 사유를 우선 사용).
     *       기존에는 이 전이가 승인 트랜잭션과 함께 롤백될 수 있었으나, 이제 별도 트랜잭션으로 커밋되므로
     *       실패 근거가 확실히 남는다.</li>
     *   <li>PG 호출 자체가 예외(타임아웃 등): 상태를 바꾸지 않고 그대로 전파한다 — 승인 여부가 불확실한
     *       상태에서 {@code FAILED}로 단정하면 PG는 승인인데 DB는 실패인 반대 방향 불일치를 만든다.</li>
     * </ul>
     *
     * @return 승인된 결제 식별자
     */
    @Override
    public Long confirmTossPayment(TossPaymentConfirmCommand command) {
        String paymentKey = command.paymentKey();
        String pgOrderId = command.pgOrderId();
        Integer amount = command.amount();
        MemberId memberIdVo = MemberId.of(command.memberId());

        TossConfirmationTarget target = paymentConfirmationExecutor.prepareInNewTx(memberIdVo, pgOrderId, amount);

        PgConfirmResult result = pgPaymentGateway.confirmPayment(
            target.paymentId(), paymentKey, target.pgOrderId(), target.amount()
        );

        if (!result.success()) {
            paymentConfirmationExecutor.failInNewTx(pgOrderId, result);
            throw new BusinessException(
                ErrorCode.PAYMENT_APPROVAL_FAILED,
                result.errorMessage() != null
                    ? result.errorMessage()
                    : ErrorCode.PAYMENT_APPROVAL_FAILED.getDefaultMessage()
            );
        }

        PaymentId paymentId;
        try {
            paymentId = paymentConfirmationExecutor.applyInNewTx(memberIdVo, pgOrderId, result);
        } catch (RuntimeException e) {
            // PG 승인은 이미 완료됐는데 DB 반영이 실패한 상태 — 자동 보상이 불가능하므로 수동 개입 진입점을
            // 남긴다. paymentKey로 PG 거래를 특정해 취소하거나 승인 상태를 수동 반영해야 한다.
            log.error(
                "{} 토스 승인 성공 후 DB 반영 실패 — pgOrderId={}, paymentKey={}, amount={}",
                PG_DB_MISMATCH, pgOrderId, result.paymentKey(), amount, e
            );
            throw e;
        }

        log.info("토스 결제 승인 완료 — paymentId={}, amount={}", paymentId.value(), amount);
        return paymentId.value();
    }

    /**
     * 현장결제를 완료 처리한다.
     *
     * <p>PG를 거치지 않는 결제 수단이라 외부 호출이 없고, 단일 트랜잭션으로 둔다.
     *
     * @return 완료된 결제 식별자
     */
    @Transactional
    @Override
    public Long completeOnSitePayment(PaymentOnSiteCompleteCommand command) {
        MemberId memberIdVo = MemberId.of(command.memberId());
        PaymentId paymentId = PaymentId.of(command.paymentId());
        return paymentConfirmationService.completeOnSitePayment(memberIdVo, paymentId).value();
    }

    /**
     * 결제를 취소한다 — 사전 판정(tx) → PG 취소 요청(tx 밖) → 결과 반영(tx) 3단으로 수행한다.
     *
     * <p>실패 시맨틱은 트랜잭션 재배치 이전과 동일하다.
     * <ul>
     *   <li>취소 불가(조리 시작·이미 취소·주문 완료): PG를 호출하지 않고 판정 코드를 그대로 돌려주며 어떤
     *       상태도 바꾸지 않는다.</li>
     *   <li>PG 취소 실패 또는 예외: {@code CANCEL_FAILED}를 돌려주며 어떤 상태도 바꾸지 않는다(예외를
     *       실패로 간주하는 것도 기존과 동일 — 과거 도메인 서비스의 {@code requestPgCancel}이 하던 처리를
     *       그대로 이 계층으로 옮겼다).</li>
     * </ul>
     *
     * <p>취소 불가 사유·PG 취소 실패는 예외가 아니라 코드로 돌아오므로(도메인 서비스 계약), 실패 코드는
     * 운영 추적을 위해 여기서 기록한다.
     *
     * @return 취소 결과 코드·메시지를 담은 응답
     */
    @Override
    public PaymentCancelResult cancelPayment(PaymentCancelCommand command) {
        PaymentCancelCode cancelCode = doCancelPayment(command.memberId(), command.paymentId(), command.cancelReason());
        return new PaymentCancelResult(cancelCode.name(), cancelCode.getMessage());
    }

    /**
     * 결제 취소를 수행하고 결과 코드를 돌려준다 — 위 {@link #cancelPayment}의 본문으로, 다중 return
     * 지점과 실패 로그를 그대로 유지하기 위해 분리했다.
     */
    private PaymentCancelCode doCancelPayment(Long memberId, Long id, String cancelReason) {
        MemberId memberIdVo = MemberId.of(memberId);
        PaymentId paymentId = PaymentId.of(id);

        PaymentCancellationTarget target = paymentCancellationExecutor.prepareInNewTx(memberIdVo, paymentId);
        if (target.isRejected()) {
            log.error("결제 취소 실패 — paymentId={}, cancelCode={}", id, target.rejectCode());
            return target.rejectCode();
        }

        if (target.pgCancelRequired() && !requestPgCancel(target.pgTid(), cancelReason)) {
            log.error("결제 취소 실패 — paymentId={}, cancelCode={}", id, PaymentCancelCode.CANCEL_FAILED);
            return PaymentCancelCode.CANCEL_FAILED;
        }

        PaymentCancelCode cancelCode;
        try {
            cancelCode = paymentCancellationExecutor.applyInNewTx(memberIdVo, paymentId, cancelReason);
        } catch (RuntimeException e) {
            if (target.pgCancelRequired()) {
                log.error(
                    "{} PG 취소 성공 후 DB 반영 실패 — paymentId={}, pgTid={}",
                    PG_DB_MISMATCH, id, target.pgTid(), e
                );
            }
            throw e;
        }

        if (cancelCode != PaymentCancelCode.SUCCESS) {
            // PG 취소가 이미 성공한 뒤 재판정에서 거절된 경우(왕복 중 주문 상태 변경) — DB는 미취소이므로
            // PG와 어긋난다. 자동 보상이 불가능해 수동 개입 진입점을 남긴다.
            if (target.pgCancelRequired()) {
                log.error(
                    "{} PG 취소 성공 후 재판정 거절 — paymentId={}, pgTid={}, cancelCode={}",
                    PG_DB_MISMATCH, id, target.pgTid(), cancelCode
                );
            }
            log.error("결제 취소 실패 — paymentId={}, cancelCode={}", id, cancelCode);
        }
        return cancelCode;
    }

    /**
     * 환불을 요청한다.
     *
     * <p>환불 접수는 PG 호출 없이 요청 레코드만 남기므로(실제 환불은 이벤트 구독자·운영 처리) 단일
     * 트랜잭션으로 둔다.
     *
     * @return 생성된 환불 요청 식별자
     */
    @Transactional
    @Override
    public Long requestRefund(PaymentRefundRequestCommand command) {
        MemberId memberIdVo = MemberId.of(command.memberId());
        PaymentId paymentId = PaymentId.of(command.paymentId());
        return paymentCancellationService
            .requestRefund(memberIdVo, paymentId, command.refundAmount(), command.refundReason())
            .value();
    }

    /**
     * PG 취소를 요청한다 — 실패·예외 모두 취소 불가로 간주해 {@code false}를 돌려주고, 어떤 상태도 바꾸지
     * 않는다(기존 도메인 서비스의 {@code requestPgCancel}과 동일한 처리를 트랜잭션 밖으로 옮긴 것).
     *
     * <p>예외를 삼키는 것이 여기서는 안전하다 — 호출 시점에 결제·주문은 아직 아무것도 바뀌지 않은 상태이고,
     * 취소가 반영되지 않으면 사용자는 재시도할 수 있다.
     */
    private boolean requestPgCancel(String pgTid, String cancelReason) {
        try {
            PgCancelResult cancelResult = pgPaymentGateway.cancelPayment(pgTid, cancelReason);
            if (!cancelResult.success()) {
                log.error("PG 취소 거절 — pgTid={}, errorCode={}, errorMessage={}",
                    pgTid, cancelResult.errorCode(), cancelResult.errorMessage());
            }
            return cancelResult.success();
        } catch (Exception e) {
            log.error("PG 취소 요청 예외 — pgTid={}", pgTid, e);
            return false;
        }
    }
}

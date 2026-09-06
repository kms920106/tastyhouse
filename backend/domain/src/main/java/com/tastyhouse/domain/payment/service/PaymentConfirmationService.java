package com.tastyhouse.domain.payment.service;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.model.Order;
import com.tastyhouse.domain.order.model.OrderStatus;
import com.tastyhouse.domain.order.service.OrderTransitionService;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.payment.event.PaymentCompletedEvent;
import com.tastyhouse.domain.payment.model.Payment;
import com.tastyhouse.domain.payment.model.PaymentMethod;
import com.tastyhouse.domain.payment.model.PaymentStatus;
import com.tastyhouse.domain.payment.model.PgProvider;
import com.tastyhouse.domain.payment.model.TossPaymentRecord;
import com.tastyhouse.domain.payment.port.dto.PgConfirmResult;
import com.tastyhouse.domain.payment.port.dto.TossPaymentDetail;
import com.tastyhouse.domain.payment.repository.PaymentRepository;
import com.tastyhouse.domain.payment.repository.TossPaymentRecordRepository;
import com.tastyhouse.domain.payment.vo.Amount;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.domain.payment.vo.PgOrderId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

/**
 * 결제 개시·승인 불변식(도메인 서비스).
 *
 * <p>결제 승인은 결제 애그리거트({@link Payment})의 상태 전이와 주문 애그리거트({@link Order})의 확정
 * 전이를 한 트랜잭션에서 반드시 함께 수행해야 하는 원자 연산이다(분류 C). 한쪽만 반영되면 "결제는 됐지만
 * 주문은 대기"이거나 "주문은 확정인데 결제는 미승인"인 정합성 붕괴가 남는다. 승인 경로는 세 가지
 * (PG 콜백 · 토스 승인 · 현장결제 완료)인데 "결제를 완료 전이하고 주문을 확정한다"는 규칙은 하나여야
 * 하므로 도메인 계층에 둔다. 결제 개시({@link #open})도 주문 상태·소유권·중복 결제라는 크로스 애그리거트
 * 불변식이라 같은 서비스가 갖는다.
 *
 * <p>주문 상태 전이는 직접 {@code order.confirm()}을 호출하지 않고 40-order의
 * {@link OrderTransitionService}에 위임한다 — 전이와 저장을 항상 함께 수행한다는 규칙의 단일 원천을
 * 주문 도메인에 유지하기 위함이다.
 *
 * <p><b>PG HTTP 왕복은 이 서비스 안에서 하지 않는다</b> — 토스 승인은 (1) 금액·상태·소유권을 검증하는
 * {@link #prepareTossConfirmation}(DB 읽기, 트랜잭션 안)과 (2) PG 응답을 반영하는
 * {@link #applyTossConfirmation}/{@link #failTossConfirmation}(DB 쓰기, 별도 트랜잭션)으로 쪼개져 있고,
 * 그 사이의 PG 호출은 소비 모듈이 <b>트랜잭션 밖에서</b> 수행한다. 과거에는 PG 왕복 전체가 DB 트랜잭션
 * 안에 있어 커넥션·행 락을 네트워크 지연만큼 점유했고, PG 승인 성공 후 커밋이 실패하면 "PG는 승인,
 * DB는 미승인"이 되어 보상이 불가능했다. 그래서 이 서비스는 {@code PgPaymentGateway}를 주입받지 않는다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code PaymentDomainConfig}가 담당한다. 트랜잭션 경계는 이 서비스를 호출하는
 * 소비 모듈의 command 서비스(web-api {@code PaymentCommandService})와 그 트랜잭션 경계 빈
 * ({@code PaymentConfirmationExecutor})이 선언한다.
 *
 * <p>이벤트 발행은 Spring {@code ApplicationEventPublisher}가 아니라 프레임워크-프리 포트
 * {@link DomainEventPublisher}를 쓴다.
 *
 * <p>도메인 모델이 순수 POJO라 더티 체킹이 없으므로 전이 후 명시적으로 {@code save}를 호출한다.
 *
 * <p>반환은 결제 식별자({@link PaymentId})만이다 — 응답 조립은 커밋 이후 소비 모듈의
 * {@code PaymentQueryService}가 infra query DAO로 재조회해 담당한다(CQRS 분리).
 */
public class PaymentConfirmationService {

    /** 현장 현금·카드 결제의 포인트 적립률(%). */
    private static final int CASH_POINT_EARN_RATE = 10;

    private final PaymentRepository paymentRepository;
    private final TossPaymentRecordRepository tossPaymentRecordRepository;
    private final OrderTransitionService orderTransitionService;
    private final DomainEventPublisher domainEventPublisher;

    public PaymentConfirmationService(
        PaymentRepository paymentRepository,
        TossPaymentRecordRepository tossPaymentRecordRepository,
        OrderTransitionService orderTransitionService,
        DomainEventPublisher domainEventPublisher
    ) {
        this.paymentRepository = paymentRepository;
        this.tossPaymentRecordRepository = tossPaymentRecordRepository;
        this.orderTransitionService = orderTransitionService;
        this.domainEventPublisher = domainEventPublisher;
    }

    /**
     * 결제를 개시한다 — 주문 소유권·결제 가능 상태·중복 결제를 검증하고 결제 금액을 주문의 최종 결제액으로
     * 고정한다.
     *
     * @return 생성된 결제 식별자
     */
    public PaymentId open(MemberId memberId, OrderId orderId, PaymentMethod paymentMethod) {
        Order order = orderTransitionService.loadOwnedBy(orderId, memberId, ErrorCode.PAYMENT_ORDER_ACCESS_DENIED);

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new BusinessException(ErrorCode.PAYMENT_INVALID_ORDER_STATUS);
        }

        if (paymentRepository.existsByOrderId(orderId)) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_IN_PROGRESS);
        }

        Payment payment = Payment.create(
            orderId,
            paymentMethod,
            new Amount(order.getFinalAmount()),
            PgOrderId.generate()
        );
        return paymentRepository.save(payment).getPaymentId();
    }

    /**
     * PG 콜백으로 통보된 승인 결과를 반영한다 — 결제를 완료 전이하고 주문을 확정한다.
     *
     * <p>PG사가 서버 간 통신으로 호출하는 경로라 회원 스코프가 없어 소유권을 검증하지 않는다(기존 동작 보존).
     *
     * @return 승인된 결제 식별자
     */
    public PaymentId confirm(PaymentId paymentId, PgConfirmation confirmation) {
        Payment payment = loadPendingPayment(paymentId);
        Order order = orderTransitionService.load(payment.getOrderId());

        payment.updatePgInfo(confirmation.pgProvider(), confirmation.pgTid(), confirmation.pgOrderId());

        if (confirmation.cardCompany() != null) {
            payment.updateCardInfo(
                confirmation.cardCompany(),
                confirmation.cardNumber(),
                confirmation.installmentMonths()
            );
        }

        payment.complete(confirmation.pgTid(), LocalDateTime.now(), confirmation.receiptUrl());

        Payment savedPayment = paymentRepository.save(payment);
        orderTransitionService.confirm(order);

        return savedPayment.getPaymentId();
    }

    /**
     * 토스 승인 1단 — PG 호출 <b>전에</b> 소유권·상태·금액을 검증하고, PG 요청에 필요한 값만 확정해 돌려준다.
     *
     * <p>검증 순서·예외 코드는 PG 호출을 트랜잭션 안에서 하던 기존 구현과 동일하다(미존재 →
     * {@code PAYMENT_NOT_FOUND}, 소유권 → {@code PAYMENT_ACCESS_DENIED}, 상태 →
     * {@code PAYMENT_NOT_PENDING_APPROVAL}, 금액 → {@code PAYMENT_AMOUNT_MISMATCH}). 즉 <b>PG를 호출하기 전에
     * 걸러지던 실패는 여전히 PG 호출 없이 같은 코드로 걸러진다.</b>
     *
     * <p>이 메서드는 읽기만 하므로 여기서 예외가 나면 바꿀 상태가 없다.
     */
    public TossConfirmationTarget prepareTossConfirmation(MemberId memberId, String pgOrderId, int amount) {
        Payment payment = paymentRepository.findByPgOrderId(pgOrderId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PAYMENT_NOT_FOUND));

        orderTransitionService.loadOwnedBy(payment.getOrderId(), memberId, ErrorCode.PAYMENT_ACCESS_DENIED);

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_PENDING_APPROVAL);
        }

        if (!payment.getAmount().value().equals(amount)) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        return new TossConfirmationTarget(payment.getId(), pgOrderId, amount);
    }

    /**
     * 토스 승인 2단(성공) — PG가 승인한 결과를 반영한다. 결제를 완료 전이하고 주문을 확정하며, PG 응답
     * 원본을 원장({@link TossPaymentRecord})에 기록한다.
     *
     * <p>새 트랜잭션에서 결제·주문을 <b>다시 로드</b>한다 — 1단에서 읽은 인스턴스는 트랜잭션이 이미 끝나
     * detached이고, PG 왕복 동안 상태가 바뀌었을 수 있기 때문이다. 그래서 상태를 여기서 <b>한 번 더</b>
     * 확인해, 왕복 중에 이미 승인·취소된 결제에 승인을 덮어쓰지 않는다(1단에만 검사를 두면 그 경합이 열린다).
     *
     * @return 승인된 결제 식별자
     */
    public PaymentId applyTossConfirmation(MemberId memberId, String pgOrderId, PgConfirmResult result) {
        Payment payment = paymentRepository.findByPgOrderId(pgOrderId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PAYMENT_NOT_FOUND));

        Order order = orderTransitionService.loadOwnedBy(
            payment.getOrderId(), memberId, ErrorCode.PAYMENT_ACCESS_DENIED
        );

        tossPaymentRecordRepository.save(toTossPaymentRecord(payment.getPaymentId(), result.detail()));

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_PENDING_APPROVAL);
        }

        payment.updatePgInfo(PgProvider.TOSS, result.paymentKey(), pgOrderId);

        if (result.cardCompany() != null) {
            payment.updateCardInfo(result.cardCompany(), result.cardNumber(), result.installmentPlanMonths());
        }

        payment.complete(result.paymentKey(), result.approvedAt(), result.receiptUrl());

        Payment savedPayment = paymentRepository.save(payment);
        orderTransitionService.confirm(order);

        domainEventPublisher.publish(new PaymentCompletedEvent(
            savedPayment.getPaymentId(),
            savedPayment.getOrderId(),
            memberId,
            savedPayment.getAmount(),
            savedPayment.getPaymentMethod(),
            false,
            savedPayment.getApprovedAt()
        ));

        return savedPayment.getPaymentId();
    }

    /**
     * 토스 승인 2단(실패) — PG가 승인을 거절한 결과를 반영한다. PG 응답 원본을 원장에 기록하고 결제를
     * {@code FAILED}로 전이해 저장한다.
     *
     * <p>원장 기록을 성공·실패 무관하게 남기는 것은 기존 동작 그대로다 — 승인 실패 시에도 PG와의 대조
     * 근거가 필요하다. 상태 전이는 커밋되어야 하므로(실패 사실을 남긴다) 예외를 던지지 않고,
     * {@code PAYMENT_APPROVAL_FAILED} 예외 변환은 트랜잭션 밖의 호출자가 커밋 이후에 수행한다 — 이 안에서
     * 던지면 원장 기록과 {@code FAILED} 전이가 함께 롤백되어 실패 근거가 사라진다.
     */
    public void failTossConfirmation(String pgOrderId, PgConfirmResult result) {
        Payment payment = paymentRepository.findByPgOrderId(pgOrderId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PAYMENT_NOT_FOUND));

        tossPaymentRecordRepository.save(toTossPaymentRecord(payment.getPaymentId(), result.detail()));

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            return;
        }

        payment.fail();
        paymentRepository.save(payment);
    }

    /**
     * 현장결제(현금·카드)를 완료 처리한다 — 결제를 완료 전이하고, 적립 예정 포인트를 주문에 반영한 뒤
     * 주문을 확정한다.
     *
     * <p>주문에 기록하는 적립 포인트와 실제 포인트 적립(이벤트 리스너 경유)은 같은 적립률
     * ({@link #CASH_POINT_EARN_RATE})을 따라야 하므로 계산식을 {@link #calculateEarnedPoint}로 모아 둔다.
     *
     * @return 완료된 결제 식별자
     */
    public PaymentId completeOnSitePayment(MemberId memberId, PaymentId paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PAYMENT_NOT_FOUND));

        Order order = orderTransitionService.loadOwnedBy(
            payment.getOrderId(), memberId, ErrorCode.PAYMENT_ACCESS_DENIED
        );

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_PENDING);
        }

        if (!isOnSitePayment(payment.getPaymentMethod())) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_ON_SITE);
        }

        LocalDateTime now = LocalDateTime.now();
        payment.complete(null, now, null);
        order.updateEarnedPoint(calculateEarnedPoint(payment.getAmount()));

        Payment savedPayment = paymentRepository.save(payment);
        orderTransitionService.confirm(order);

        domainEventPublisher.publish(new PaymentCompletedEvent(
            savedPayment.getPaymentId(),
            savedPayment.getOrderId(),
            memberId,
            savedPayment.getAmount(),
            savedPayment.getPaymentMethod(),
            true,
            now
        ));

        return savedPayment.getPaymentId();
    }

    /**
     * 현장 결제 적립 포인트를 계산한다 — 주문 반영({@link #completeOnSitePayment})과 실제 적립
     * (이벤트 리스너 경유 {@code PointLedgerService})이 같은 값을 쓰도록 계산식의 단일 원천이 된다.
     */
    public int calculateEarnedPoint(Amount amount) {
        return (int) (amount.value() * CASH_POINT_EARN_RATE / 100.0);
    }

    /**
     * 현장 결제 적립률(%) — 적립 이력 문구에 함께 남긴다.
     */
    public int earnRate() {
        return CASH_POINT_EARN_RATE;
    }

    /**
     * 승인 대기 중인 결제를 로드한다 — 없으면 미존재, 대기 상태가 아니면 승인 불가로 실패시킨다.
     */
    private Payment loadPendingPayment(PaymentId paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_PENDING_APPROVAL);
        }
        return payment;
    }

    private boolean isOnSitePayment(PaymentMethod paymentMethod) {
        return paymentMethod == PaymentMethod.CASH_ON_SITE || paymentMethod == PaymentMethod.CARD_ON_SITE;
    }

    /**
     * PG 응답 상세를 원장 애그리거트로 변환한다.
     */
    private TossPaymentRecord toTossPaymentRecord(PaymentId paymentId, TossPaymentDetail detail) {
        return TossPaymentRecord.create(
            paymentId,
            detail.version(),
            detail.paymentKey(),
            detail.type(),
            detail.orderId(),
            detail.orderName(),
            detail.mId(),
            detail.currency(),
            detail.method(),
            detail.totalAmount(),
            detail.balanceAmount(),
            detail.status(),
            detail.requestedAt(),
            detail.approvedAt(),
            detail.useEscrow(),
            detail.lastTransactionKey(),
            detail.suppliedAmount(),
            detail.vat(),
            detail.cultureExpense(),
            detail.taxFreeAmount(),
            detail.taxExemptionAmount(),
            detail.partialCancelable(),
            detail.cardAmount(),
            detail.cardIssuerCode(),
            detail.cardAcquirerCode(),
            detail.cardNumber(),
            detail.cardInstallmentPlanMonths(),
            detail.cardApproveNo(),
            detail.cardUseCardPoint(),
            detail.cardType(),
            detail.cardOwnerType(),
            detail.cardAcquireStatus(),
            detail.cardInterestFree(),
            detail.cardInterestPayer(),
            detail.virtualAccountType(),
            detail.virtualAccountNumber(),
            detail.virtualAccountBankCode(),
            detail.virtualAccountCustomerName(),
            detail.virtualAccountDueDate(),
            detail.virtualAccountRefundStatus(),
            detail.virtualAccountExpired(),
            detail.virtualAccountSettlementStatus(),
            detail.mobilePhoneCustomerMobilePhone(),
            detail.mobilePhoneSettlementStatus(),
            detail.mobilePhoneReceiptUrl(),
            detail.transferBankCode(),
            detail.transferSettlementStatus(),
            detail.easyPayProvider(),
            detail.easyPayAmount(),
            detail.easyPayDiscountAmount(),
            detail.receiptUrl(),
            detail.checkoutUrl(),
            detail.failureCode(),
            detail.failureMessage(),
            detail.country()
        );
    }
}

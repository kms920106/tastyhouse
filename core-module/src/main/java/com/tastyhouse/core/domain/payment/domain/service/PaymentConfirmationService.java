package com.tastyhouse.core.domain.payment.domain.service;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.order.domain.model.Order;
import com.tastyhouse.core.domain.order.domain.model.OrderStatus;
import com.tastyhouse.core.domain.order.domain.service.OrderTransitionService;
import com.tastyhouse.core.domain.order.domain.vo.OrderId;
import com.tastyhouse.core.domain.payment.domain.event.PaymentCompletedEvent;
import com.tastyhouse.core.domain.payment.domain.model.Payment;
import com.tastyhouse.core.domain.payment.domain.model.PaymentMethod;
import com.tastyhouse.core.domain.payment.domain.model.PaymentStatus;
import com.tastyhouse.core.domain.payment.domain.model.PgProvider;
import com.tastyhouse.core.domain.payment.domain.model.TossPaymentRecord;
import com.tastyhouse.core.domain.payment.domain.port.PgPaymentGateway;
import com.tastyhouse.core.domain.payment.domain.port.dto.PgConfirmResult;
import com.tastyhouse.core.domain.payment.domain.port.dto.TossPaymentDetail;
import com.tastyhouse.core.domain.payment.domain.repository.PaymentRepository;
import com.tastyhouse.core.domain.payment.domain.repository.TossPaymentRecordRepository;
import com.tastyhouse.core.domain.payment.domain.vo.Amount;
import com.tastyhouse.core.domain.payment.domain.vo.PaymentId;
import com.tastyhouse.core.domain.payment.domain.vo.PgOrderId;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.shared.event.DomainEventPublisher;

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
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code DomainServiceConfig}가 담당한다. 트랜잭션 경계는 이 서비스를 호출하는
 * 소비 모듈의 command 서비스(web-api {@code PaymentCommandService})가 선언한다.
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
    private final PgPaymentGateway pgPaymentGateway;
    private final OrderTransitionService orderTransitionService;
    private final DomainEventPublisher domainEventPublisher;

    public PaymentConfirmationService(
        PaymentRepository paymentRepository,
        TossPaymentRecordRepository tossPaymentRecordRepository,
        PgPaymentGateway pgPaymentGateway,
        OrderTransitionService orderTransitionService,
        DomainEventPublisher domainEventPublisher
    ) {
        this.paymentRepository = paymentRepository;
        this.tossPaymentRecordRepository = tossPaymentRecordRepository;
        this.pgPaymentGateway = pgPaymentGateway;
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
     * 토스페이먼츠 결제를 승인한다 — 금액을 대조하고 PG 승인을 요청한 뒤, 성공 시 결제를 완료 전이하고
     * 주문을 확정한다.
     *
     * <p>PG 응답 원본은 성공·실패와 무관하게 원장({@link TossPaymentRecord})에 먼저 기록한다 — 승인 실패
     * 시에도 PG와의 대조 근거를 남겨야 하기 때문이다. 실패 시 결제를 {@code FAILED}로 전이해 저장한 뒤
     * {@code PAYMENT_APPROVAL_FAILED}로 실패시킨다.
     *
     * @return 승인된 결제 식별자
     */
    public PaymentId confirmTossPayment(MemberId memberId, String paymentKey, String pgOrderId, int amount) {
        Payment payment = paymentRepository.findByPgOrderId(pgOrderId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "결제를 찾을 수 없습니다."));

        Order order = orderTransitionService.loadOwnedBy(
            payment.getOrderId(), memberId, ErrorCode.PAYMENT_ACCESS_DENIED
        );

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_PENDING_APPROVAL);
        }

        if (!payment.getAmount().value().equals(amount)) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        PgConfirmResult result = pgPaymentGateway.confirmPayment(payment.getId(), paymentKey, pgOrderId, amount);
        tossPaymentRecordRepository.save(toTossPaymentRecord(payment.getId(), result.detail()));

        if (!result.success()) {
            payment.fail();
            paymentRepository.save(payment);
            throw new BusinessException(
                ErrorCode.PAYMENT_APPROVAL_FAILED,
                result.errorMessage() != null
                    ? result.errorMessage()
                    : ErrorCode.PAYMENT_APPROVAL_FAILED.getDefaultMessage()
            );
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
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "결제를 찾을 수 없습니다."));

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
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "결제를 찾을 수 없습니다."));

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
    private TossPaymentRecord toTossPaymentRecord(Long paymentId, TossPaymentDetail detail) {
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

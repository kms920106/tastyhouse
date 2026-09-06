package com.tastyhouse.domain.payment.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.payment.vo.Amount;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.domain.payment.vo.PgOrderId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 결제 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code PaymentJpaEntity} + {@code PaymentMapper}가 담당한다. 도메인이 프레임워크-프리이므로
 * 변경 후 저장은 더티 체킹이 아니라 command 서비스가 명시적으로 {@code PaymentRepository#save}를
 * 호출해야 한다.
 */
public class Payment {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final OrderId orderId; // 결제 대상 주문 ID
    private final PaymentMethod paymentMethod; // 결제 수단
    private PaymentStatus paymentStatus; // 결제 상태
    private final Amount amount; // 결제 금액
    private PgProvider pgProvider; // PG사
    private String pgTid; // PG 거래 ID
    private String pgOrderId; // PG 주문 ID
    private String cardCompany; // 카드사
    private String cardNumber; // 카드 번호(마스킹)
    private Integer installmentMonths; // 할부 개월 수
    private LocalDateTime approvedAt; // 승인 시각
    private LocalDateTime cancelledAt; // 취소 시각
    private String cancelReason; // 취소 사유
    private String receiptUrl; // 영수증 URL
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private Payment(
        Long id,
        OrderId orderId,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        Amount amount,
        PgProvider pgProvider,
        String pgTid,
        String pgOrderId,
        String cardCompany,
        String cardNumber,
        Integer installmentMonths,
        LocalDateTime approvedAt,
        LocalDateTime cancelledAt,
        String cancelReason,
        String receiptUrl,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.amount = amount;
        this.pgProvider = pgProvider;
        this.pgTid = pgTid;
        this.pgOrderId = pgOrderId;
        this.cardCompany = cardCompany;
        this.cardNumber = cardNumber;
        this.installmentMonths = installmentMonths;
        this.approvedAt = approvedAt;
        this.cancelledAt = cancelledAt;
        this.cancelReason = cancelReason;
        this.receiptUrl = receiptUrl;
        this.createdAt = createdAt;
    }

    /**
     * 신규 결제를 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     */
    public static Payment create(OrderId orderId, PaymentMethod paymentMethod, Amount amount, PgOrderId pgOrderId) {
        return new Payment(
            null,
            orderId,
            paymentMethod,
            PaymentStatus.PENDING,
            amount,
            null,
            null,
            pgOrderId.value(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
    public static Payment reconstitute(
        Long id,
        OrderId orderId,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        Amount amount,
        PgProvider pgProvider,
        String pgTid,
        String pgOrderId,
        String cardCompany,
        String cardNumber,
        Integer installmentMonths,
        LocalDateTime approvedAt,
        LocalDateTime cancelledAt,
        String cancelReason,
        String receiptUrl,
        LocalDateTime createdAt
    ) {
        return new Payment(
            id,
            orderId,
            paymentMethod,
            paymentStatus,
            amount,
            pgProvider,
            pgTid,
            pgOrderId,
            cardCompany,
            cardNumber,
            installmentMonths,
            approvedAt,
            cancelledAt,
            cancelReason,
            receiptUrl,
            createdAt
        );
    }

    public Long getId() {
        return this.id;
    }

    public OrderId getOrderId() {
        return this.orderId;
    }

    public PaymentMethod getPaymentMethod() {
        return this.paymentMethod;
    }

    public PaymentStatus getPaymentStatus() {
        return this.paymentStatus;
    }

    public Amount getAmount() {
        return this.amount;
    }

    public PgProvider getPgProvider() {
        return this.pgProvider;
    }

    public String getPgTid() {
        return this.pgTid;
    }

    public String getPgOrderId() {
        return this.pgOrderId;
    }

    public String getCardCompany() {
        return this.cardCompany;
    }

    public String getCardNumber() {
        return this.cardNumber;
    }

    public Integer getInstallmentMonths() {
        return this.installmentMonths;
    }

    public LocalDateTime getApprovedAt() {
        return this.approvedAt;
    }

    public LocalDateTime getCancelledAt() {
        return this.cancelledAt;
    }

    public String getCancelReason() {
        return this.cancelReason;
    }

    public String getReceiptUrl() {
        return this.receiptUrl;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public PaymentId getPaymentId() {
        return PaymentId.of(this.id);
    }

    public void complete(String pgTid, LocalDateTime approvedAt, String receiptUrl) {
        if (this.paymentStatus != PaymentStatus.PENDING) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_PENDING);
        }
        this.paymentStatus = PaymentStatus.COMPLETED;
        this.pgTid = pgTid;
        this.approvedAt = approvedAt;
        this.receiptUrl = receiptUrl;
    }

    public void fail() {
        this.paymentStatus = PaymentStatus.FAILED;
    }

    public void cancel(String cancelReason, LocalDateTime now) {
        this.paymentStatus = PaymentStatus.CANCELLED;
        this.cancelledAt = now;
        this.cancelReason = cancelReason;
    }

    public void updatePgInfo(PgProvider pgProvider, String pgTid, String pgOrderId) {
        this.pgProvider = pgProvider;
        this.pgTid = pgTid;
        this.pgOrderId = pgOrderId;
    }

    public void updateCardInfo(String cardCompany, String cardNumber, Integer installmentMonths) {
        this.cardCompany = cardCompany;
        this.cardNumber = cardNumber;
        this.installmentMonths = installmentMonths;
    }
}

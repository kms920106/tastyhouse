package com.tastyhouse.infrastructure.payment.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.payment.model.PaymentMethod;
import com.tastyhouse.domain.payment.model.PaymentStatus;
import com.tastyhouse.domain.payment.model.PgProvider;
import com.tastyhouse.domain.payment.vo.Amount;
import com.tastyhouse.infrastructure.order.persistence.OrderIdConverter;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 결제 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code Payment}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code PaymentMapper}가 수행한다.
 */
@Entity
@Table(name = "PAYMENT")
public class PaymentJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = OrderIdConverter.class)
    @Column(name = "order_id", nullable = false, unique = true)
    private OrderId orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30, columnDefinition = "VARCHAR(30)")
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private PaymentStatus paymentStatus;

    @Convert(converter = AmountConverter.class)
    @Column(name = "amount", nullable = false)
    private Amount amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "pg_provider", length = 30, columnDefinition = "VARCHAR(30)")
    private PgProvider pgProvider;

    @Column(name = "pg_tid", length = 100)
    private String pgTid;

    @Column(name = "pg_order_id", length = 100)
    private String pgOrderId;

    @Column(name = "card_company", length = 50)
    private String cardCompany;

    @Column(name = "card_number", length = 30)
    private String cardNumber;

    @Column(name = "installment_months")
    private Integer installmentMonths;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @Column(name = "receipt_url", length = 500)
    private String receiptUrl;

    protected PaymentJpaEntity() {
    }

    private PaymentJpaEntity(
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
        String receiptUrl
    ) {
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
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code PaymentMapper#toEntity}에서만 호출한다.
     */
    static PaymentJpaEntity create(
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
        String receiptUrl
    ) {
        return new PaymentJpaEntity(
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
            receiptUrl
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자는 건드리지 않는다.
     */
    void applyChanges(
        PaymentStatus paymentStatus,
        PgProvider pgProvider,
        String pgTid,
        String pgOrderId,
        String cardCompany,
        String cardNumber,
        Integer installmentMonths,
        LocalDateTime approvedAt,
        LocalDateTime cancelledAt,
        String cancelReason,
        String receiptUrl
    ) {
        this.paymentStatus = paymentStatus;
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
}

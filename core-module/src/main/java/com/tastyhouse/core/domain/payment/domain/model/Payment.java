package com.tastyhouse.core.domain.payment.domain.model;

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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.core.domain.order.domain.vo.OrderId;
import com.tastyhouse.core.domain.order.infrastructure.persistence.converter.OrderIdConverter;
import com.tastyhouse.core.domain.payment.domain.vo.Amount;
import com.tastyhouse.core.domain.payment.domain.vo.PgOrderId;
import com.tastyhouse.core.domain.payment.infrastructure.persistence.converter.AmountConverter;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.shared.entity.BaseEntity;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "PAYMENT")
public class Payment extends BaseEntity {

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

    private Payment(
        OrderId orderId,
        PaymentMethod paymentMethod,
        Amount amount,
        PgOrderId pgOrderId
    ) {
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = PaymentStatus.PENDING;
        this.amount = amount;
        this.pgOrderId = pgOrderId.value();
    }

    public static Payment create(OrderId orderId, PaymentMethod paymentMethod, Amount amount, PgOrderId pgOrderId) {
        return new Payment(orderId, paymentMethod, amount, pgOrderId);
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

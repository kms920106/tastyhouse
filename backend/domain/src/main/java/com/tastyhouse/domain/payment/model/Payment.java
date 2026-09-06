package com.tastyhouse.domain.payment.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.payment.vo.Amount;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.domain.payment.vo.PgOrderId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public class Payment {
    private final Long id;
    private final OrderId orderId;
    private final PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private final Amount amount;
    private PgProvider pgProvider;
    private String pgTid;
    private String pgOrderId;
    private String cardCompany;
    private String cardNumber;
    private Integer installmentMonths;
    private LocalDateTime approvedAt;
    private LocalDateTime cancelledAt;
    private String cancelReason;
    private String receiptUrl;
    private final LocalDateTime createdAt;

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

package com.tastyhouse.core.entity.payment;

import com.tastyhouse.core.entity.BaseEntity;
import jakarta.persistence.Column;
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

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "PAYMENT")
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId; // 주문 ID (ORDERS.id 참조)

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30, columnDefinition = "VARCHAR(30)")
    private PaymentMethod paymentMethod; // 결제 수단 (CARD: 카드, VIRTUAL_ACCOUNT: 가상계좌, EASY_PAY: 간편결제 등)

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private PaymentStatus paymentStatus; // 결제 상태 (PENDING: 대기, COMPLETED: 완료, FAILED: 실패, CANCELLED: 취소)

    @Column(name = "amount", nullable = false)
    private Integer amount; // 결제 금액

    @Enumerated(EnumType.STRING)
    @Column(name = "pg_provider", length = 30, columnDefinition = "VARCHAR(30)")
    private PgProvider pgProvider; // PG사 (TOSS: 토스페이먼츠 등)

    @Column(name = "pg_tid", length = 100)
    private String pgTid; // PG사 거래 ID (결제 승인 후 PG사에서 발급)

    @Column(name = "pg_order_id", length = 100)
    private String pgOrderId; // PG사에 전달한 주문 ID

    @Column(name = "card_company", length = 50)
    private String cardCompany; // 카드사명

    @Column(name = "card_number", length = 30)
    private String cardNumber; // 카드번호 (일부 마스킹)

    @Column(name = "installment_months")
    private Integer installmentMonths; // 할부 개월 수 (0이면 일시불)

    @Column(name = "approved_at")
    private LocalDateTime approvedAt; // 결제 승인 일시

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt; // 결제 취소 일시

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason; // 결제 취소 사유

    @Column(name = "receipt_url", length = 500)
    private String receiptUrl; // 영수증 URL

    @Column(name = "cash_receipt_number", length = 50)
    private String cashReceiptNumber; // 현금영수증 발급 번호

    @Column(name = "cash_receipt_type", length = 20)
    private String cashReceiptType; // 현금영수증 종류 (소득공제, 지출증빙)

    private Payment(
        Long orderId,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        Integer amount,
        PgProvider pgProvider,
        String pgTid,
        String pgOrderId,
        String cardCompany,
        String cardNumber,
        Integer installmentMonths,
        LocalDateTime approvedAt,
        String receiptUrl,
        String cashReceiptNumber,
        String cashReceiptType
    ) {
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus != null ? paymentStatus : PaymentStatus.PENDING;
        this.amount = amount != null ? amount : 0;
        this.pgProvider = pgProvider;
        this.pgTid = pgTid;
        this.pgOrderId = pgOrderId;
        this.cardCompany = cardCompany;
        this.cardNumber = cardNumber;
        this.installmentMonths = installmentMonths;
        this.approvedAt = approvedAt;
        this.receiptUrl = receiptUrl;
        this.cashReceiptNumber = cashReceiptNumber;
        this.cashReceiptType = cashReceiptType;
    }

    public static Payment of(
        Long orderId,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        Integer amount,
        PgProvider pgProvider,
        String pgTid,
        String pgOrderId,
        String cardCompany,
        String cardNumber,
        Integer installmentMonths,
        LocalDateTime approvedAt,
        String receiptUrl,
        String cashReceiptNumber,
        String cashReceiptType
    ) {
        return new Payment(
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
            receiptUrl,
            cashReceiptNumber,
            cashReceiptType
        );
    }

    public void complete(String pgTid, LocalDateTime approvedAt, String receiptUrl) {
        this.paymentStatus = PaymentStatus.COMPLETED;
        this.pgTid = pgTid;
        this.approvedAt = approvedAt;
        this.receiptUrl = receiptUrl;
    }

    public void fail() {
        this.paymentStatus = PaymentStatus.FAILED;
    }

    public void cancel(String cancelReason) {
        this.paymentStatus = PaymentStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
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

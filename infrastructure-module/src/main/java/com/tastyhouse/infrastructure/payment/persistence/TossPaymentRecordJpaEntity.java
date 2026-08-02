package com.tastyhouse.infrastructure.payment.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.payment.domain.vo.PaymentId;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 토스페이먼츠 결제 원장(raw) JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code TossPaymentRecord}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code TossPaymentRecordMapper}가 수행한다.
 * update 경로가 없는 insert 전용 애그리거트라 {@code applyChanges}는 두지 않는다.
 */
@Entity
@Table(name = "TOSS_PAYMENT_RECORD")
public class TossPaymentRecordJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = PaymentIdConverter.class)
    @Column(name = "payment_id", nullable = false)
    private PaymentId paymentId;

    @Column(name = "version", length = 20)
    private String version;

    @Column(name = "payment_key", length = 200)
    private String paymentKey;

    @Column(name = "type", length = 20)
    private String type;

    @Column(name = "order_id", length = 64)
    private String orderId;

    @Column(name = "order_name", length = 100)
    private String orderName;

    @Column(name = "m_id", length = 14)
    private String mId;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "method", length = 30)
    private String method;

    @Column(name = "total_amount")
    private Integer totalAmount;

    @Column(name = "balance_amount")
    private Integer balanceAmount;

    @Column(name = "status", length = 30)
    private String status;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "use_escrow")
    private boolean useEscrow;

    @Column(name = "last_transaction_key", length = 64)
    private String lastTransactionKey;

    @Column(name = "supplied_amount")
    private Integer suppliedAmount;

    @Column(name = "vat")
    private Integer vat;

    @Column(name = "culture_expense")
    private boolean cultureExpense;

    @Column(name = "tax_free_amount")
    private Integer taxFreeAmount;

    @Column(name = "tax_exemption_amount")
    private Integer taxExemptionAmount;

    @Column(name = "is_partial_cancelable")
    private boolean partialCancelable;

    @Column(name = "card_amount")
    private Integer cardAmount;

    @Column(name = "card_issuer_code", length = 10)
    private String cardIssuerCode;

    @Column(name = "card_acquirer_code", length = 10)
    private String cardAcquirerCode;

    @Column(name = "card_number", length = 20)
    private String cardNumber;

    @Column(name = "card_installment_plan_months")
    private Integer cardInstallmentPlanMonths;

    @Column(name = "card_approve_no", length = 8)
    private String cardApproveNo;

    @Column(name = "card_use_card_point")
    private boolean cardUseCardPoint;

    @Column(name = "card_type", length = 20)
    private String cardType;

    @Column(name = "card_owner_type", length = 20)
    private String cardOwnerType;

    @Column(name = "card_acquire_status", length = 30)
    private String cardAcquireStatus;

    @Column(name = "card_is_interest_free")
    private boolean cardInterestFree;

    @Column(name = "card_interest_payer", length = 20)
    private String cardInterestPayer;

    @Column(name = "virtual_account_type", length = 20)
    private String virtualAccountType;

    @Column(name = "virtual_account_number", length = 20)
    private String virtualAccountNumber;

    @Column(name = "virtual_account_bank_code", length = 10)
    private String virtualAccountBankCode;

    @Column(name = "virtual_account_customer_name", length = 100)
    private String virtualAccountCustomerName;

    @Column(name = "virtual_account_due_date")
    private LocalDateTime virtualAccountDueDate;

    @Column(name = "virtual_account_refund_status", length = 30)
    private String virtualAccountRefundStatus;

    @Column(name = "virtual_account_expired")
    private boolean virtualAccountExpired;

    @Column(name = "virtual_account_settlement_status", length = 30)
    private String virtualAccountSettlementStatus;

    @Column(name = "mobile_phone_customer_mobile_phone", length = 15)
    private String mobilePhoneCustomerMobilePhone;

    @Column(name = "mobile_phone_settlement_status", length = 30)
    private String mobilePhoneSettlementStatus;

    @Column(name = "mobile_phone_receipt_url", length = 500)
    private String mobilePhoneReceiptUrl;

    @Column(name = "transfer_bank_code", length = 10)
    private String transferBankCode;

    @Column(name = "transfer_settlement_status", length = 30)
    private String transferSettlementStatus;

    @Column(name = "receipt_url", length = 500)
    private String receiptUrl;

    @Column(name = "checkout_url", length = 500)
    private String checkoutUrl;

    @Column(name = "easy_pay_provider", length = 30)
    private String easyPayProvider;

    @Column(name = "easy_pay_amount")
    private Integer easyPayAmount;

    @Column(name = "easy_pay_discount_amount")
    private Integer easyPayDiscountAmount;

    @Column(name = "country", length = 2)
    private String country;

    @Column(name = "failure_code", length = 50)
    private String failureCode;

    @Column(name = "failure_message", length = 510)
    private String failureMessage;

    protected TossPaymentRecordJpaEntity() {
    }

    private TossPaymentRecordJpaEntity(PaymentId paymentId, String version, String paymentKey, String type,
                              String orderId, String orderName, String mId, String currency,
                              String method, Integer totalAmount, Integer balanceAmount, String status,
                              LocalDateTime requestedAt, LocalDateTime approvedAt, boolean useEscrow,
                              String lastTransactionKey, Integer suppliedAmount, Integer vat,
                              boolean cultureExpense, Integer taxFreeAmount, Integer taxExemptionAmount,
                              boolean partialCancelable, Integer cardAmount, String cardIssuerCode,
                              String cardAcquirerCode, String cardNumber, Integer cardInstallmentPlanMonths,
                              String cardApproveNo, boolean cardUseCardPoint, String cardType,
                              String cardOwnerType, String cardAcquireStatus, boolean cardInterestFree,
                              String cardInterestPayer, String virtualAccountType, String virtualAccountNumber,
                              String virtualAccountBankCode, String virtualAccountCustomerName,
                              LocalDateTime virtualAccountDueDate, String virtualAccountRefundStatus,
                              boolean virtualAccountExpired, String virtualAccountSettlementStatus,
                              String mobilePhoneCustomerMobilePhone, String mobilePhoneSettlementStatus,
                              String mobilePhoneReceiptUrl, String transferBankCode,
                              String transferSettlementStatus, String easyPayProvider, Integer easyPayAmount,
                              Integer easyPayDiscountAmount, String receiptUrl, String checkoutUrl,
                              String failureCode, String failureMessage, String country) {
        this.paymentId = paymentId;
        this.version = version;
        this.paymentKey = paymentKey;
        this.type = type;
        this.orderId = orderId;
        this.orderName = orderName;
        this.mId = mId;
        this.currency = currency;
        this.method = method;
        this.totalAmount = totalAmount;
        this.balanceAmount = balanceAmount;
        this.status = status;
        this.requestedAt = requestedAt;
        this.approvedAt = approvedAt;
        this.useEscrow = useEscrow;
        this.lastTransactionKey = lastTransactionKey;
        this.suppliedAmount = suppliedAmount;
        this.vat = vat;
        this.cultureExpense = cultureExpense;
        this.taxFreeAmount = taxFreeAmount;
        this.taxExemptionAmount = taxExemptionAmount;
        this.partialCancelable = partialCancelable;
        this.cardAmount = cardAmount;
        this.cardIssuerCode = cardIssuerCode;
        this.cardAcquirerCode = cardAcquirerCode;
        this.cardNumber = cardNumber;
        this.cardInstallmentPlanMonths = cardInstallmentPlanMonths;
        this.cardApproveNo = cardApproveNo;
        this.cardUseCardPoint = cardUseCardPoint;
        this.cardType = cardType;
        this.cardOwnerType = cardOwnerType;
        this.cardAcquireStatus = cardAcquireStatus;
        this.cardInterestFree = cardInterestFree;
        this.cardInterestPayer = cardInterestPayer;
        this.virtualAccountType = virtualAccountType;
        this.virtualAccountNumber = virtualAccountNumber;
        this.virtualAccountBankCode = virtualAccountBankCode;
        this.virtualAccountCustomerName = virtualAccountCustomerName;
        this.virtualAccountDueDate = virtualAccountDueDate;
        this.virtualAccountRefundStatus = virtualAccountRefundStatus;
        this.virtualAccountExpired = virtualAccountExpired;
        this.virtualAccountSettlementStatus = virtualAccountSettlementStatus;
        this.mobilePhoneCustomerMobilePhone = mobilePhoneCustomerMobilePhone;
        this.mobilePhoneSettlementStatus = mobilePhoneSettlementStatus;
        this.mobilePhoneReceiptUrl = mobilePhoneReceiptUrl;
        this.transferBankCode = transferBankCode;
        this.transferSettlementStatus = transferSettlementStatus;
        this.easyPayProvider = easyPayProvider;
        this.easyPayAmount = easyPayAmount;
        this.easyPayDiscountAmount = easyPayDiscountAmount;
        this.receiptUrl = receiptUrl;
        this.checkoutUrl = checkoutUrl;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        this.country = country;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code TossPaymentRecordMapper#toEntity}에서만 호출한다.
     */
    static TossPaymentRecordJpaEntity create(PaymentId paymentId, String version, String paymentKey, String type,
                                           String orderId, String orderName, String mId, String currency,
                                           String method, Integer totalAmount, Integer balanceAmount, String status,
                                           LocalDateTime requestedAt, LocalDateTime approvedAt, boolean useEscrow,
                                           String lastTransactionKey, Integer suppliedAmount, Integer vat,
                                           boolean cultureExpense, Integer taxFreeAmount, Integer taxExemptionAmount,
                                           boolean partialCancelable, Integer cardAmount, String cardIssuerCode,
                                           String cardAcquirerCode, String cardNumber, Integer cardInstallmentPlanMonths,
                                           String cardApproveNo, boolean cardUseCardPoint, String cardType,
                                           String cardOwnerType, String cardAcquireStatus, boolean cardInterestFree,
                                           String cardInterestPayer, String virtualAccountType, String virtualAccountNumber,
                                           String virtualAccountBankCode, String virtualAccountCustomerName,
                                           LocalDateTime virtualAccountDueDate, String virtualAccountRefundStatus,
                                           boolean virtualAccountExpired, String virtualAccountSettlementStatus,
                                           String mobilePhoneCustomerMobilePhone, String mobilePhoneSettlementStatus,
                                           String mobilePhoneReceiptUrl, String transferBankCode,
                                           String transferSettlementStatus, String easyPayProvider, Integer easyPayAmount,
                                           Integer easyPayDiscountAmount, String receiptUrl, String checkoutUrl,
                                           String failureCode, String failureMessage, String country) {
        return new TossPaymentRecordJpaEntity(paymentId, version, paymentKey, type, orderId, orderName, mId, currency,
            method, totalAmount, balanceAmount, status, requestedAt, approvedAt, useEscrow, lastTransactionKey,
            suppliedAmount, vat, cultureExpense, taxFreeAmount, taxExemptionAmount, partialCancelable, cardAmount,
            cardIssuerCode, cardAcquirerCode, cardNumber, cardInstallmentPlanMonths, cardApproveNo, cardUseCardPoint,
            cardType, cardOwnerType, cardAcquireStatus, cardInterestFree, cardInterestPayer, virtualAccountType,
            virtualAccountNumber, virtualAccountBankCode, virtualAccountCustomerName, virtualAccountDueDate,
            virtualAccountRefundStatus, virtualAccountExpired, virtualAccountSettlementStatus,
            mobilePhoneCustomerMobilePhone, mobilePhoneSettlementStatus, mobilePhoneReceiptUrl, transferBankCode,
            transferSettlementStatus, easyPayProvider, easyPayAmount, easyPayDiscountAmount, receiptUrl, checkoutUrl,
            failureCode, failureMessage, country);
    }

    public Long getId() {
        return this.id;
    }

    public PaymentId getPaymentId() {
        return this.paymentId;
    }

    public String getVersion() {
        return this.version;
    }

    public String getPaymentKey() {
        return this.paymentKey;
    }

    public String getType() {
        return this.type;
    }

    public String getOrderId() {
        return this.orderId;
    }

    public String getOrderName() {
        return this.orderName;
    }

    public String getMId() {
        return this.mId;
    }

    public String getCurrency() {
        return this.currency;
    }

    public String getMethod() {
        return this.method;
    }

    public Integer getTotalAmount() {
        return this.totalAmount;
    }

    public Integer getBalanceAmount() {
        return this.balanceAmount;
    }

    public String getStatus() {
        return this.status;
    }

    public LocalDateTime getRequestedAt() {
        return this.requestedAt;
    }

    public LocalDateTime getApprovedAt() {
        return this.approvedAt;
    }

    public boolean isUseEscrow() {
        return this.useEscrow;
    }

    public String getLastTransactionKey() {
        return this.lastTransactionKey;
    }

    public Integer getSuppliedAmount() {
        return this.suppliedAmount;
    }

    public Integer getVat() {
        return this.vat;
    }

    public boolean isCultureExpense() {
        return this.cultureExpense;
    }

    public Integer getTaxFreeAmount() {
        return this.taxFreeAmount;
    }

    public Integer getTaxExemptionAmount() {
        return this.taxExemptionAmount;
    }

    public boolean isPartialCancelable() {
        return this.partialCancelable;
    }

    public Integer getCardAmount() {
        return this.cardAmount;
    }

    public String getCardIssuerCode() {
        return this.cardIssuerCode;
    }

    public String getCardAcquirerCode() {
        return this.cardAcquirerCode;
    }

    public String getCardNumber() {
        return this.cardNumber;
    }

    public Integer getCardInstallmentPlanMonths() {
        return this.cardInstallmentPlanMonths;
    }

    public String getCardApproveNo() {
        return this.cardApproveNo;
    }

    public boolean isCardUseCardPoint() {
        return this.cardUseCardPoint;
    }

    public String getCardType() {
        return this.cardType;
    }

    public String getCardOwnerType() {
        return this.cardOwnerType;
    }

    public String getCardAcquireStatus() {
        return this.cardAcquireStatus;
    }

    public boolean isCardInterestFree() {
        return this.cardInterestFree;
    }

    public String getCardInterestPayer() {
        return this.cardInterestPayer;
    }

    public String getVirtualAccountType() {
        return this.virtualAccountType;
    }

    public String getVirtualAccountNumber() {
        return this.virtualAccountNumber;
    }

    public String getVirtualAccountBankCode() {
        return this.virtualAccountBankCode;
    }

    public String getVirtualAccountCustomerName() {
        return this.virtualAccountCustomerName;
    }

    public LocalDateTime getVirtualAccountDueDate() {
        return this.virtualAccountDueDate;
    }

    public String getVirtualAccountRefundStatus() {
        return this.virtualAccountRefundStatus;
    }

    public boolean isVirtualAccountExpired() {
        return this.virtualAccountExpired;
    }

    public String getVirtualAccountSettlementStatus() {
        return this.virtualAccountSettlementStatus;
    }

    public String getMobilePhoneCustomerMobilePhone() {
        return this.mobilePhoneCustomerMobilePhone;
    }

    public String getMobilePhoneSettlementStatus() {
        return this.mobilePhoneSettlementStatus;
    }

    public String getMobilePhoneReceiptUrl() {
        return this.mobilePhoneReceiptUrl;
    }

    public String getTransferBankCode() {
        return this.transferBankCode;
    }

    public String getTransferSettlementStatus() {
        return this.transferSettlementStatus;
    }

    public String getReceiptUrl() {
        return this.receiptUrl;
    }

    public String getCheckoutUrl() {
        return this.checkoutUrl;
    }

    public String getEasyPayProvider() {
        return this.easyPayProvider;
    }

    public Integer getEasyPayAmount() {
        return this.easyPayAmount;
    }

    public Integer getEasyPayDiscountAmount() {
        return this.easyPayDiscountAmount;
    }

    public String getCountry() {
        return this.country;
    }

    public String getFailureCode() {
        return this.failureCode;
    }

    public String getFailureMessage() {
        return this.failureMessage;
    }
}

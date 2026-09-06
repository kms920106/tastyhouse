package com.tastyhouse.domain.payment.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.payment.vo.PaymentId;

/**
 * 토스페이먼츠 결제 원장(raw) 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code TossPaymentRecordJpaEntity} + {@code TossPaymentRecordMapper}가 담당한다. 상태전이
 * 메서드가 없는 insert 전용 레코드이므로 신규 생성 시에도 감사 시각을 요구하지 않는다.
 */
public class TossPaymentRecord {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final PaymentId paymentId;
    private final String version;
    private final String paymentKey;
    private final String type;
    private final String orderId;
    private final String orderName;
    private final String mId;
    private final String currency;
    private final String method;
    private final Integer totalAmount;
    private final Integer balanceAmount;
    private final String status;
    private final LocalDateTime requestedAt;
    private final LocalDateTime approvedAt;
    private final boolean useEscrow;
    private final String lastTransactionKey;
    private final Integer suppliedAmount;
    private final Integer vat;
    private final boolean cultureExpense;
    private final Integer taxFreeAmount;
    private final Integer taxExemptionAmount;
    private final boolean partialCancelable;
    private final Integer cardAmount;
    private final String cardIssuerCode;
    private final String cardAcquirerCode;
    private final String cardNumber;
    private final Integer cardInstallmentPlanMonths;
    private final String cardApproveNo;
    private final boolean cardUseCardPoint;
    private final String cardType;
    private final String cardOwnerType;
    private final String cardAcquireStatus;
    private final boolean cardInterestFree;
    private final String cardInterestPayer;
    private final String virtualAccountType;
    private final String virtualAccountNumber;
    private final String virtualAccountBankCode;
    private final String virtualAccountCustomerName;
    private final LocalDateTime virtualAccountDueDate;
    private final String virtualAccountRefundStatus;
    private final boolean virtualAccountExpired;
    private final String virtualAccountSettlementStatus;
    private final String mobilePhoneCustomerMobilePhone;
    private final String mobilePhoneSettlementStatus;
    private final String mobilePhoneReceiptUrl;
    private final String transferBankCode;
    private final String transferSettlementStatus;
    private final String receiptUrl;
    private final String checkoutUrl;
    private final String easyPayProvider;
    private final Integer easyPayAmount;
    private final Integer easyPayDiscountAmount;
    private final String country;
    private final String failureCode;
    private final String failureMessage;
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private TossPaymentRecord(
        Long id, PaymentId paymentId, String version, String paymentKey, String type,
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
        String failureCode, String failureMessage, String country,
        LocalDateTime createdAt
    ) {
        this.id = id;
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
        this.createdAt = createdAt;
    }

    /**
     * 신규 토스 결제 원장을 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     */
    public static TossPaymentRecord create(
        PaymentId paymentId, String version, String paymentKey, String type,
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
        String failureCode, String failureMessage, String country
    ) {
        return new TossPaymentRecord(
            null, paymentId, version, paymentKey, type, orderId, orderName, mId, currency,
            method, totalAmount, balanceAmount, status, requestedAt, approvedAt, useEscrow, lastTransactionKey,
            suppliedAmount, vat, cultureExpense, taxFreeAmount, taxExemptionAmount, partialCancelable, cardAmount,
            cardIssuerCode, cardAcquirerCode, cardNumber, cardInstallmentPlanMonths, cardApproveNo, cardUseCardPoint,
            cardType, cardOwnerType, cardAcquireStatus, cardInterestFree, cardInterestPayer, virtualAccountType,
            virtualAccountNumber, virtualAccountBankCode, virtualAccountCustomerName, virtualAccountDueDate,
            virtualAccountRefundStatus, virtualAccountExpired, virtualAccountSettlementStatus,
            mobilePhoneCustomerMobilePhone, mobilePhoneSettlementStatus, mobilePhoneReceiptUrl, transferBankCode,
            transferSettlementStatus, easyPayProvider, easyPayAmount, easyPayDiscountAmount, receiptUrl, checkoutUrl,
            failureCode, failureMessage, country, null
        );
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
    public static TossPaymentRecord reconstitute(
        Long id, PaymentId paymentId, String version, String paymentKey, String type,
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
        String failureCode, String failureMessage, String country,
        LocalDateTime createdAt
    ) {
        return new TossPaymentRecord(
            id, paymentId, version, paymentKey, type, orderId, orderName, mId, currency,
            method, totalAmount, balanceAmount, status, requestedAt, approvedAt, useEscrow, lastTransactionKey,
            suppliedAmount, vat, cultureExpense, taxFreeAmount, taxExemptionAmount, partialCancelable, cardAmount,
            cardIssuerCode, cardAcquirerCode, cardNumber, cardInstallmentPlanMonths, cardApproveNo, cardUseCardPoint,
            cardType, cardOwnerType, cardAcquireStatus, cardInterestFree, cardInterestPayer, virtualAccountType,
            virtualAccountNumber, virtualAccountBankCode, virtualAccountCustomerName, virtualAccountDueDate,
            virtualAccountRefundStatus, virtualAccountExpired, virtualAccountSettlementStatus,
            mobilePhoneCustomerMobilePhone, mobilePhoneSettlementStatus, mobilePhoneReceiptUrl, transferBankCode,
            transferSettlementStatus, easyPayProvider, easyPayAmount, easyPayDiscountAmount, receiptUrl, checkoutUrl,
            failureCode, failureMessage, country, createdAt
        );
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

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}

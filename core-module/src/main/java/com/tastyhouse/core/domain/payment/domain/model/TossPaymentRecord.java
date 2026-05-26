package com.tastyhouse.core.domain.payment.domain.model;

import com.tastyhouse.core.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "TOSS_PAYMENT_RECORD")
public class TossPaymentRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

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
    private Boolean useEscrow;

    @Column(name = "last_transaction_key", length = 64)
    private String lastTransactionKey;

    @Column(name = "supplied_amount")
    private Integer suppliedAmount;

    @Column(name = "vat")
    private Integer vat;

    @Column(name = "culture_expense")
    private Boolean cultureExpense;

    @Column(name = "tax_free_amount")
    private Integer taxFreeAmount;

    @Column(name = "tax_exemption_amount")
    private Integer taxExemptionAmount;

    @Column(name = "is_partial_cancelable")
    private Boolean isPartialCancelable;

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
    private Boolean cardUseCardPoint;

    @Column(name = "card_type", length = 20)
    private String cardType;

    @Column(name = "card_owner_type", length = 20)
    private String cardOwnerType;

    @Column(name = "card_acquire_status", length = 30)
    private String cardAcquireStatus;

    @Column(name = "card_is_interest_free")
    private Boolean cardIsInterestFree;

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

    @Column(name = "virtual_account_depositor_name", length = 100)
    private String virtualAccountDepositorName;

    @Column(name = "virtual_account_due_date")
    private LocalDateTime virtualAccountDueDate;

    @Column(name = "virtual_account_refund_status", length = 30)
    private String virtualAccountRefundStatus;

    @Column(name = "virtual_account_expired")
    private Boolean virtualAccountExpired;

    @Column(name = "virtual_account_settlement_status", length = 30)
    private String virtualAccountSettlementStatus;

    @Column(name = "mobile_phone_customer_mobile_phone", length = 15)
    private String mobilePhoneCustomerMobilePhone;

    @Column(name = "mobile_phone_settlement_status", length = 30)
    private String mobilePhoneSettlementStatus;

    @Column(name = "mobile_phone_receipt_url", length = 500)
    private String mobilePhoneReceiptUrl;

    @Column(name = "gift_certificate_approve_no", length = 8)
    private String giftCertificateApproveNo;

    @Column(name = "gift_certificate_settlement_status", length = 30)
    private String giftCertificateSettlementStatus;

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

    @Column(name = "cash_receipt_type", length = 20)
    private String cashReceiptType;

    @Column(name = "cash_receipt_key", length = 200)
    private String cashReceiptKey;

    @Column(name = "cash_receipt_issue_number", length = 9)
    private String cashReceiptIssueNumber;

    @Column(name = "cash_receipt_url", length = 500)
    private String cashReceiptUrl;

    @Column(name = "cash_receipt_amount")
    private Integer cashReceiptAmount;

    @Column(name = "cash_receipt_tax_free_amount")
    private Integer cashReceiptTaxFreeAmount;

    @Column(name = "discount_amount")
    private Integer discountAmount;

    @Builder
    public TossPaymentRecord(Long paymentId, String version, String paymentKey, String type,
                             String orderId, String orderName, String mId, String currency,
                             String method, Integer totalAmount, Integer balanceAmount, String status,
                             LocalDateTime requestedAt, LocalDateTime approvedAt, Boolean useEscrow,
                             String lastTransactionKey, Integer suppliedAmount, Integer vat,
                             Boolean cultureExpense, Integer taxFreeAmount, Integer taxExemptionAmount,
                             Boolean isPartialCancelable, Integer cardAmount, String cardIssuerCode,
                             String cardAcquirerCode, String cardNumber, Integer cardInstallmentPlanMonths,
                             String cardApproveNo, Boolean cardUseCardPoint, String cardType,
                             String cardOwnerType, String cardAcquireStatus, Boolean cardIsInterestFree,
                             String cardInterestPayer, String virtualAccountType, String virtualAccountNumber,
                             String virtualAccountBankCode, String virtualAccountCustomerName,
                             String virtualAccountDepositorName, LocalDateTime virtualAccountDueDate,
                             String virtualAccountRefundStatus, Boolean virtualAccountExpired,
                             String virtualAccountSettlementStatus, String mobilePhoneCustomerMobilePhone,
                             String mobilePhoneSettlementStatus, String mobilePhoneReceiptUrl,
                             String giftCertificateApproveNo, String giftCertificateSettlementStatus,
                             String transferBankCode, String transferSettlementStatus, String receiptUrl,
                             String checkoutUrl, String easyPayProvider, Integer easyPayAmount,
                             Integer easyPayDiscountAmount, String country, String failureCode,
                             String failureMessage, String cashReceiptType, String cashReceiptKey,
                             String cashReceiptIssueNumber, String cashReceiptUrl, Integer cashReceiptAmount,
                             Integer cashReceiptTaxFreeAmount, Integer discountAmount) {
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
        this.isPartialCancelable = isPartialCancelable;
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
        this.cardIsInterestFree = cardIsInterestFree;
        this.cardInterestPayer = cardInterestPayer;
        this.virtualAccountType = virtualAccountType;
        this.virtualAccountNumber = virtualAccountNumber;
        this.virtualAccountBankCode = virtualAccountBankCode;
        this.virtualAccountCustomerName = virtualAccountCustomerName;
        this.virtualAccountDepositorName = virtualAccountDepositorName;
        this.virtualAccountDueDate = virtualAccountDueDate;
        this.virtualAccountRefundStatus = virtualAccountRefundStatus;
        this.virtualAccountExpired = virtualAccountExpired;
        this.virtualAccountSettlementStatus = virtualAccountSettlementStatus;
        this.mobilePhoneCustomerMobilePhone = mobilePhoneCustomerMobilePhone;
        this.mobilePhoneSettlementStatus = mobilePhoneSettlementStatus;
        this.mobilePhoneReceiptUrl = mobilePhoneReceiptUrl;
        this.giftCertificateApproveNo = giftCertificateApproveNo;
        this.giftCertificateSettlementStatus = giftCertificateSettlementStatus;
        this.transferBankCode = transferBankCode;
        this.transferSettlementStatus = transferSettlementStatus;
        this.receiptUrl = receiptUrl;
        this.checkoutUrl = checkoutUrl;
        this.easyPayProvider = easyPayProvider;
        this.easyPayAmount = easyPayAmount;
        this.easyPayDiscountAmount = easyPayDiscountAmount;
        this.country = country;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        this.cashReceiptType = cashReceiptType;
        this.cashReceiptKey = cashReceiptKey;
        this.cashReceiptIssueNumber = cashReceiptIssueNumber;
        this.cashReceiptUrl = cashReceiptUrl;
        this.cashReceiptAmount = cashReceiptAmount;
        this.cashReceiptTaxFreeAmount = cashReceiptTaxFreeAmount;
        this.discountAmount = discountAmount;
    }
}

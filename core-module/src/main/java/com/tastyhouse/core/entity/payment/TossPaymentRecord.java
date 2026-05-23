package com.tastyhouse.core.entity.payment;

import com.tastyhouse.core.entity.BaseEntity;
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
    private Long id; // PK

    @Column(name = "payment_id", nullable = false)
    private Long paymentId; // 결제 ID (PAYMENT.id 참조)

    @Column(name = "version", length = 20)
    private String version; // Toss 응답 API 버전 (날짜 기반 버저닝, 예: 2022-06-08)

    @Column(name = "payment_key", length = 200)
    private String paymentKey; // Toss 결제 키 (결제 고유 식별자, 최대 200자)

    @Column(name = "type", length = 20)
    private String type; // 결제 타입 (NORMAL: 일반결제, BILLING: 자동결제, BRANDPAY: 브랜드페이)

    @Column(name = "order_id", length = 64)
    private String orderId; // Toss에 전달한 주문번호 (영문 대소문자, 숫자, -, _ 조합 6~64자)

    @Column(name = "order_name", length = 100)
    private String orderName; // 구매 상품명 (예: 생수 외 1건)

    @Column(name = "m_id", length = 14)
    private String mId; // 상점아이디 (MID, 토스페이먼츠에서 발급)

    @Column(name = "currency", length = 10)
    private String currency; // 결제 통화 (예: KRW)

    @Column(name = "method", length = 30)
    private String method; // 결제 수단 (카드, 가상계좌, 간편결제, 휴대폰, 계좌이체 등)

    @Column(name = "total_amount")
    private Integer totalAmount; // 총 결제 금액 (취소 후에도 최초 결제 금액 유지)

    @Column(name = "balance_amount")
    private Integer balanceAmount; // 취소 가능 잔액

    @Column(name = "status", length = 30)
    private String status; // 결제 처리 상태 (READY, IN_PROGRESS, WAITING_FOR_DEPOSIT, DONE, CANCELED, PARTIAL_CANCELED, ABORTED, EXPIRED)

    @Column(name = "requested_at")
    private LocalDateTime requestedAt; // 결제 요청 일시

    @Column(name = "approved_at")
    private LocalDateTime approvedAt; // 결제 승인 일시

    @Column(name = "use_escrow")
    private Boolean useEscrow; // 에스크로 사용 여부 (true: 에스크로 사용)

    @Column(name = "last_transaction_key", length = 64)
    private String lastTransactionKey; // 마지막 거래 키값 (최대 64자)

    @Column(name = "supplied_amount")
    private Integer suppliedAmount; // 공급가액 (부가세 제외 금액)

    @Column(name = "vat")
    private Integer vat; // 부가세

    @Column(name = "culture_expense")
    private Boolean cultureExpense; // 문화비 지출 여부 (true: 문화비로 지출)

    @Column(name = "tax_free_amount")
    private Integer taxFreeAmount; // 면세 금액

    @Column(name = "tax_exemption_amount")
    private Integer taxExemptionAmount; // 과세 제외 금액 (컵 보증금 등)

    @Column(name = "is_partial_cancelable")
    private Boolean isPartialCancelable; // 부분 취소 가능 여부 (true: 부분 취소 가능)

    @Column(name = "card_amount")
    private Integer cardAmount; // 카드사에 결제 요청한 금액

    @Column(name = "card_issuer_code", length = 10)
    private String cardIssuerCode; // 카드 발급사 코드 (두 자리)

    @Column(name = "card_acquirer_code", length = 10)
    private String cardAcquirerCode; // 카드 매입사 코드 (두 자리)

    @Column(name = "card_number", length = 20)
    private String cardNumber; // 카드번호 (일부 마스킹 처리)

    @Column(name = "card_installment_plan_months")
    private Integer cardInstallmentPlanMonths; // 할부 개월 수 (0이면 일시불)

    @Column(name = "card_approve_no", length = 8)
    private String cardApproveNo; // 카드사 승인 번호 (최대 8자)

    @Column(name = "card_use_card_point")
    private Boolean cardUseCardPoint; // 카드사 포인트 사용 여부 (true: 포인트 사용)

    @Column(name = "card_type", length = 20)
    private String cardType; // 카드 종류 (신용, 체크, 기프트, 미확인)

    @Column(name = "card_owner_type", length = 20)
    private String cardOwnerType; // 카드 소유자 타입 (개인, 법인, 미확인)

    @Column(name = "card_acquire_status", length = 30)
    private String cardAcquireStatus; // 카드 매입 상태 (READY, REQUESTED, COMPLETED, CANCEL_REQUESTED, CANCELED)

    @Column(name = "card_is_interest_free")
    private Boolean cardIsInterestFree; // 무이자 할부 적용 여부 (true: 무이자 할부)

    @Column(name = "card_interest_payer", length = 20)
    private String cardInterestPayer; // 할부 수수료 부담 주체 (BUYER, CARD_COMPANY, MERCHANT)

    @Column(name = "virtual_account_type", length = 20)
    private String virtualAccountType; // 가상계좌 타입 (일반, 고정)

    @Column(name = "virtual_account_number", length = 20)
    private String virtualAccountNumber; // 발급된 가상계좌 번호

    @Column(name = "virtual_account_bank_code", length = 10)
    private String virtualAccountBankCode; // 가상계좌 은행 코드 (두 자리)

    @Column(name = "virtual_account_customer_name", length = 100)
    private String virtualAccountCustomerName; // 가상계좌를 발급한 구매자명

    @Column(name = "virtual_account_depositor_name", length = 100)
    private String virtualAccountDepositorName; // 가상계좌 입금자명

    @Column(name = "virtual_account_due_date")
    private LocalDateTime virtualAccountDueDate; // 가상계좌 입금 기한

    @Column(name = "virtual_account_refund_status", length = 30)
    private String virtualAccountRefundStatus; // 가상계좌 환불 처리 상태 (NONE, PENDING, FAILED, PARTIAL_FAILED, COMPLETED)

    @Column(name = "virtual_account_expired")
    private Boolean virtualAccountExpired; // 가상계좌 만료 여부 (true: 만료됨)

    @Column(name = "virtual_account_settlement_status", length = 30)
    private String virtualAccountSettlementStatus; // 가상계좌 정산 상태 (INCOMPLETED, COMPLETED)

    @Column(name = "mobile_phone_customer_mobile_phone", length = 15)
    private String mobilePhoneCustomerMobilePhone; // 구매자 휴대폰 번호 (휴대폰 결제 시)

    @Column(name = "mobile_phone_settlement_status", length = 30)
    private String mobilePhoneSettlementStatus; // 휴대폰 결제 정산 상태

    @Column(name = "mobile_phone_receipt_url", length = 500)
    private String mobilePhoneReceiptUrl; // 휴대폰 결제 영수증 URL

    @Column(name = "gift_certificate_approve_no", length = 8)
    private String giftCertificateApproveNo; // 상품권 결제 승인번호 (최대 8자)

    @Column(name = "gift_certificate_settlement_status", length = 30)
    private String giftCertificateSettlementStatus; // 상품권 정산 상태

    @Column(name = "transfer_bank_code", length = 10)
    private String transferBankCode; // 계좌이체 은행 코드 (두 자리)

    @Column(name = "transfer_settlement_status", length = 30)
    private String transferSettlementStatus; // 계좌이체 정산 상태

    @Column(name = "receipt_url", length = 500)
    private String receiptUrl; // 발행된 영수증 URL

    @Column(name = "checkout_url", length = 500)
    private String checkoutUrl; // 결제창 URL

    @Column(name = "easy_pay_provider", length = 30)
    private String easyPayProvider; // 간편결제사 코드

    @Column(name = "easy_pay_amount")
    private Integer easyPayAmount; // 간편결제 서비스 계좌 또는 현금성 포인트로 결제한 금액

    @Column(name = "easy_pay_discount_amount")
    private Integer easyPayDiscountAmount; // 간편결제 적립 포인트 또는 쿠폰 즉시 할인 금액

    @Column(name = "country", length = 2)
    private String country; // 결제 국가 코드 (ISO-3166 두 자리, 예: KR)

    @Column(name = "failure_code", length = 50)
    private String failureCode; // 결제 승인 실패 에러 코드

    @Column(name = "failure_message", length = 510)
    private String failureMessage; // 결제 승인 실패 에러 메시지

    @Column(name = "cash_receipt_type", length = 20)
    private String cashReceiptType; // 현금영수증 종류 (소득공제, 지출증빙)

    @Column(name = "cash_receipt_key", length = 200)
    private String cashReceiptKey; // 현금영수증 키값

    @Column(name = "cash_receipt_issue_number", length = 9)
    private String cashReceiptIssueNumber; // 현금영수증 발급 번호

    @Column(name = "cash_receipt_url", length = 500)
    private String cashReceiptUrl; // 현금영수증 확인 URL

    @Column(name = "cash_receipt_amount")
    private Integer cashReceiptAmount; // 현금영수증 처리 금액

    @Column(name = "cash_receipt_tax_free_amount")
    private Integer cashReceiptTaxFreeAmount; // 현금영수증 면세 처리 금액

    @Column(name = "discount_amount")
    private Integer discountAmount; // 카드사 및 퀵계좌이체 즉시 할인 프로모션 적용 금액

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

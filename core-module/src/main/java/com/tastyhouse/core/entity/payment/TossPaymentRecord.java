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
    private Long id;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId; // Payment Entity와 연결

    // Payment 객체의 응답 버전입니다. 버전 2022-06-08부터 날짜 기반 버저닝을 사용합니다.
    @Column(name = "version", length = 20)
    private String version;

    // 결제의 키값입니다. 최대 길이는 200자입니다. 결제를 식별하는 역할로, 중복되지 않는 고유한 값입니다.
    @Column(name = "payment_key", length = 200)
    private String paymentKey;

    // 결제 타입 정보입니다. NORMAL(일반결제), BILLING(자동결제), BRANDPAY(브랜드페이) 중 하나입니다.
    @Column(name = "type", length = 20)
    private String type;

    // 주문번호입니다. 결제 요청에서 내 상점이 직접 생성한 영문 대소문자, 숫자, 특수문자 -, _로 이루어진 6자 이상 64자 이하의 문자열입니다.
    @Column(name = "order_id", length = 64)
    private String orderId;

    // 구매상품입니다. 예를 들면 생수 외 1건 같은 형식입니다. 최대 길이는 100자입니다.
    @Column(name = "order_name", length = 100)
    private String orderName;

    // 상점아이디(MID)입니다. 토스페이먼츠에서 발급합니다. 최대 길이는 14자입니다.
    @Column(name = "m_id", length = 14)
    private String mId;

    // 결제할 때 사용한 통화입니다.
    @Column(name = "currency", length = 10)
    private String currency;

    // 결제수단입니다. 카드, 가상계좌, 간편결제, 휴대폰, 계좌이체, 문화상품권, 도서문화상품권, 게임문화상품권 중 하나입니다.
    @Column(name = "method", length = 30)
    private String method;

    // 총 결제 금액입니다. 결제가 취소되는 등 결제 상태가 변해도 최초에 결제된 결제 금액으로 유지됩니다.
    @Column(name = "total_amount")
    private Integer totalAmount;

    // 취소할 수 있는 금액(잔고)입니다.
    @Column(name = "balance_amount")
    private Integer balanceAmount;

    // 결제 처리 상태입니다. READY, IN_PROGRESS, WAITING_FOR_DEPOSIT, DONE, CANCELED, PARTIAL_CANCELED, ABORTED, EXPIRED 중 하나입니다.
    @Column(name = "status", length = 30)
    private String status;

    // 결제가 일어난 날짜와 시간 정보입니다.
    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    // 결제 승인이 일어난 날짜와 시간 정보입니다.
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    // 에스크로 사용 여부입니다.
    @Column(name = "use_escrow")
    private Boolean useEscrow;

    // 마지막 거래의 키값입니다. 최대 길이는 64자입니다.
    @Column(name = "last_transaction_key", length = 64)
    private String lastTransactionKey;

    // 공급가액입니다.
    @Column(name = "supplied_amount")
    private Integer suppliedAmount;

    // 부가세입니다.
    @Column(name = "vat")
    private Integer vat;

    // 문화비 지출 여부입니다.
    @Column(name = "culture_expense")
    private Boolean cultureExpense;

    // 결제 금액 중 면세 금액입니다.
    @Column(name = "tax_free_amount")
    private Integer taxFreeAmount;

    // 과세를 제외한 결제 금액(컵 보증금 등)입니다.
    @Column(name = "tax_exemption_amount")
    private Integer taxExemptionAmount;

    // 부분 취소 가능 여부입니다.
    @Column(name = "is_partial_cancelable")
    private Boolean isPartialCancelable;

    // 카드사에 결제 요청한 금액입니다.
    @Column(name = "card_amount")
    private Integer cardAmount;

    // 카드 발급사 두 자리 코드입니다.
    @Column(name = "card_issuer_code", length = 10)
    private String cardIssuerCode;

    // 카드 매입사 두 자리 코드입니다.
    @Column(name = "card_acquirer_code", length = 10)
    private String cardAcquirerCode;

    // 카드번호입니다. 번호의 일부는 마스킹 되어 있습니다.
    @Column(name = "card_number", length = 20)
    private String cardNumber;

    // 할부 개월 수입니다. 일시불이면 0입니다.
    @Column(name = "card_installment_plan_months")
    private Integer cardInstallmentPlanMonths;

    // 카드사 승인 번호입니다. 최대 길이는 8자입니다.
    @Column(name = "card_approve_no", length = 8)
    private String cardApproveNo;

    // 카드사 포인트 사용 여부입니다.
    @Column(name = "card_use_card_point")
    private Boolean cardUseCardPoint;

    // 카드 종류입니다. 신용, 체크, 기프트, 미확인 중 하나입니다.
    @Column(name = "card_type", length = 20)
    private String cardType;

    // 카드의 소유자 타입입니다. 개인, 법인, 미확인 중 하나입니다.
    @Column(name = "card_owner_type", length = 20)
    private String cardOwnerType;

    // 카드 결제의 매입 상태입니다. READY, REQUESTED, COMPLETED, CANCEL_REQUESTED, CANCELED 중 하나입니다.
    @Column(name = "card_acquire_status", length = 30)
    private String cardAcquireStatus;

    // 무이자 할부의 적용 여부입니다.
    @Column(name = "card_is_interest_free")
    private Boolean cardIsInterestFree;

    // 할부 수수료를 부담하는 주체입니다. BUYER, CARD_COMPANY, MERCHANT 중 하나입니다.
    @Column(name = "card_interest_payer", length = 20)
    private String cardInterestPayer;

    // 가상계좌 타입을 나타냅니다. 일반, 고정 중 하나입니다.
    @Column(name = "virtual_account_type", length = 20)
    private String virtualAccountType;

    // 발급된 계좌번호입니다.
    @Column(name = "virtual_account_number", length = 20)
    private String virtualAccountNumber;

    // 가상계좌 은행 두 자리 코드입니다.
    @Column(name = "virtual_account_bank_code", length = 10)
    private String virtualAccountBankCode;

    // 가상계좌를 발급한 구매자명입니다.
    @Column(name = "virtual_account_customer_name", length = 100)
    private String virtualAccountCustomerName;

    // 가상계좌에 입금한 계좌의 입금자명입니다.
    @Column(name = "virtual_account_depositor_name", length = 100)
    private String virtualAccountDepositorName;

    // 입금 기한입니다.
    @Column(name = "virtual_account_due_date")
    private LocalDateTime virtualAccountDueDate;

    // 환불 처리 상태입니다. NONE, PENDING, FAILED, PARTIAL_FAILED, COMPLETED 중 하나입니다.
    @Column(name = "virtual_account_refund_status", length = 30)
    private String virtualAccountRefundStatus;

    // 가상계좌의 만료 여부입니다.
    @Column(name = "virtual_account_expired")
    private Boolean virtualAccountExpired;

    // 정산 상태입니다. INCOMPLETED, COMPLETED 중 하나입니다.
    @Column(name = "virtual_account_settlement_status", length = 30)
    private String virtualAccountSettlementStatus;

    // 구매자가 결제에 사용한 휴대폰 번호입니다.
    @Column(name = "mobile_phone_customer_mobile_phone", length = 15)
    private String mobilePhoneCustomerMobilePhone;

    // 휴대폰 정산 상태입니다.
    @Column(name = "mobile_phone_settlement_status", length = 30)
    private String mobilePhoneSettlementStatus;

    // 휴대폰 결제 내역 영수증을 확인할 수 있는 주소입니다.
    @Column(name = "mobile_phone_receipt_url", length = 500)
    private String mobilePhoneReceiptUrl;

    // 상품권 결제 승인번호입니다.
    @Column(name = "gift_certificate_approve_no", length = 8)
    private String giftCertificateApproveNo;

    // 상품권 정산 상태입니다.
    @Column(name = "gift_certificate_settlement_status", length = 30)
    private String giftCertificateSettlementStatus;

    // 계좌이체 은행 두 자리 코드입니다.
    @Column(name = "transfer_bank_code", length = 10)
    private String transferBankCode;

    // 계좌이체 정산 상태입니다.
    @Column(name = "transfer_settlement_status", length = 30)
    private String transferSettlementStatus;

    // 발행된 영수증 URL입니다.
    @Column(name = "receipt_url", length = 500)
    private String receiptUrl;

    // 결제창이 열리는 주소입니다.
    @Column(name = "checkout_url", length = 500)
    private String checkoutUrl;

    // 선택한 간편결제사 코드입니다.
    @Column(name = "easy_pay_provider", length = 30)
    private String easyPayProvider;

    // 간편결제 서비스에 등록된 계좌 혹은 현금성 포인트로 결제한 금액입니다.
    @Column(name = "easy_pay_amount")
    private Integer easyPayAmount;

    // 간편결제 서비스의 적립 포인트나 쿠폰 등으로 즉시 할인된 금액입니다.
    @Column(name = "easy_pay_discount_amount")
    private Integer easyPayDiscountAmount;

    // 결제한 국가입니다. ISO-3166의 두 자리 국가 코드 형식입니다.
    @Column(name = "country", length = 2)
    private String country;

    // 결제 승인 실패 오류 타입을 보여주는 에러 코드입니다.
    @Column(name = "failure_code", length = 50)
    private String failureCode;

    // 에러 메시지입니다.
    @Column(name = "failure_message", length = 510)
    private String failureMessage;

    // 현금영수증의 종류입니다. 소득공제, 지출증빙 중 하나입니다.
    @Column(name = "cash_receipt_type", length = 20)
    private String cashReceiptType;

    // 현금영수증의 키값입니다.
    @Column(name = "cash_receipt_key", length = 200)
    private String cashReceiptKey;

    // 현금영수증 발급 번호입니다.
    @Column(name = "cash_receipt_issue_number", length = 9)
    private String cashReceiptIssueNumber;

    // 발행된 현금영수증을 확인할 수 있는 주소입니다.
    @Column(name = "cash_receipt_url", length = 500)
    private String cashReceiptUrl;

    // 현금영수증 처리된 금액입니다.
    @Column(name = "cash_receipt_amount")
    private Integer cashReceiptAmount;

    // 현금영수증 면세 처리된 금액입니다.
    @Column(name = "cash_receipt_tax_free_amount")
    private Integer cashReceiptTaxFreeAmount;

    // 카드사 및 퀵계좌이체의 즉시 할인 프로모션을 적용한 결제 금액입니다.
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

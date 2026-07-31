package com.tastyhouse.core.domain.payment.domain.port.dto;

import java.time.LocalDateTime;

/**
 * 거래 기록(TossPaymentRecord) 저장에 필요한 PG 원본 상세 데이터.
 * core가 external DTO에 의존하지 않도록 표준 타입의 평면 필드로 구성한다.
 * paymentId는 core가 이미 보유하므로 포함하지 않는다.
 */
public record TossPaymentDetail(
    String version,
    String paymentKey,
    String type,
    String orderId,
    String orderName,
    String mId,
    String currency,
    String method,
    Integer totalAmount,
    Integer balanceAmount,
    String status,
    LocalDateTime requestedAt,
    LocalDateTime approvedAt,
    boolean useEscrow,
    String lastTransactionKey,
    Integer suppliedAmount,
    Integer vat,
    boolean cultureExpense,
    Integer taxFreeAmount,
    Integer taxExemptionAmount,
    boolean partialCancelable,
    Integer cardAmount,
    String cardIssuerCode,
    String cardAcquirerCode,
    String cardNumber,
    Integer cardInstallmentPlanMonths,
    String cardApproveNo,
    boolean cardUseCardPoint,
    String cardType,
    String cardOwnerType,
    String cardAcquireStatus,
    boolean cardInterestFree,
    String cardInterestPayer,
    String virtualAccountType,
    String virtualAccountNumber,
    String virtualAccountBankCode,
    String virtualAccountCustomerName,
    LocalDateTime virtualAccountDueDate,
    String virtualAccountRefundStatus,
    boolean virtualAccountExpired,
    String virtualAccountSettlementStatus,
    String mobilePhoneCustomerMobilePhone,
    String mobilePhoneSettlementStatus,
    String mobilePhoneReceiptUrl,
    String transferBankCode,
    String transferSettlementStatus,
    String easyPayProvider,
    Integer easyPayAmount,
    Integer easyPayDiscountAmount,
    String receiptUrl,
    String checkoutUrl,
    String failureCode,
    String failureMessage,
    String country
) {
}

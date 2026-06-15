package com.tastyhouse.core.domain.payment.application.port.dto;

import java.time.LocalDateTime;

public record PgConfirmResult(
    boolean success,
    String paymentKey,
    String status,
    Integer totalAmount,
    LocalDateTime approvedAt,
    String receiptUrl,
    String cardCompany,
    String cardNumber,
    Integer installmentPlanMonths,
    String errorCode,
    String errorMessage,
    TossPaymentDetail detail
) {

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
        Boolean useEscrow,
        String lastTransactionKey,
        Integer suppliedAmount,
        Integer vat,
        Boolean cultureExpense,
        Integer taxFreeAmount,
        Integer taxExemptionAmount,
        Boolean isPartialCancelable,
        Integer cardAmount,
        String cardIssuerCode,
        String cardAcquirerCode,
        String cardNumber,
        Integer cardInstallmentPlanMonths,
        String cardApproveNo,
        Boolean cardUseCardPoint,
        String cardType,
        String cardOwnerType,
        String cardAcquireStatus,
        Boolean cardIsInterestFree,
        String cardInterestPayer,
        String virtualAccountType,
        String virtualAccountNumber,
        String virtualAccountBankCode,
        String virtualAccountCustomerName,
        LocalDateTime virtualAccountDueDate,
        String virtualAccountRefundStatus,
        Boolean virtualAccountExpired,
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
}

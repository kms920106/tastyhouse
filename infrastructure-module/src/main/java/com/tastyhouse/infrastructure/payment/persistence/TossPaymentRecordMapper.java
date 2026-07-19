package com.tastyhouse.infrastructure.payment.persistence;

import com.tastyhouse.core.domain.payment.domain.model.TossPaymentRecord;

/**
 * 토스페이먼츠 결제 원장 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 *
 * <p>필드가 60여 개로 많아 순서 착오 위험이 크다. {@code TossPaymentRecord} 필드 선언 순서 ·
 * {@code reconstitute}/{@code create} 파라미터 순서 · 이 매퍼가 넘기는 인자 순서를 3중 대조했다.
 */
final class TossPaymentRecordMapper {

    private TossPaymentRecordMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static TossPaymentRecord toDomain(TossPaymentRecordJpaEntity entity) {
        return TossPaymentRecord.reconstitute(
            entity.getId(),
            entity.getPaymentId(),
            entity.getVersion(),
            entity.getPaymentKey(),
            entity.getType(),
            entity.getOrderId(),
            entity.getOrderName(),
            entity.getMId(),
            entity.getCurrency(),
            entity.getMethod(),
            entity.getTotalAmount(),
            entity.getBalanceAmount(),
            entity.getStatus(),
            entity.getRequestedAt(),
            entity.getApprovedAt(),
            entity.isUseEscrow(),
            entity.getLastTransactionKey(),
            entity.getSuppliedAmount(),
            entity.getVat(),
            entity.isCultureExpense(),
            entity.getTaxFreeAmount(),
            entity.getTaxExemptionAmount(),
            entity.isPartialCancelable(),
            entity.getCardAmount(),
            entity.getCardIssuerCode(),
            entity.getCardAcquirerCode(),
            entity.getCardNumber(),
            entity.getCardInstallmentPlanMonths(),
            entity.getCardApproveNo(),
            entity.isCardUseCardPoint(),
            entity.getCardType(),
            entity.getCardOwnerType(),
            entity.getCardAcquireStatus(),
            entity.isCardInterestFree(),
            entity.getCardInterestPayer(),
            entity.getVirtualAccountType(),
            entity.getVirtualAccountNumber(),
            entity.getVirtualAccountBankCode(),
            entity.getVirtualAccountCustomerName(),
            entity.getVirtualAccountDueDate(),
            entity.getVirtualAccountRefundStatus(),
            entity.isVirtualAccountExpired(),
            entity.getVirtualAccountSettlementStatus(),
            entity.getMobilePhoneCustomerMobilePhone(),
            entity.getMobilePhoneSettlementStatus(),
            entity.getMobilePhoneReceiptUrl(),
            entity.getTransferBankCode(),
            entity.getTransferSettlementStatus(),
            entity.getEasyPayProvider(),
            entity.getEasyPayAmount(),
            entity.getEasyPayDiscountAmount(),
            entity.getReceiptUrl(),
            entity.getCheckoutUrl(),
            entity.getFailureCode(),
            entity.getFailureMessage(),
            entity.getCountry(),
            entity.getCreatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static TossPaymentRecordJpaEntity toEntity(TossPaymentRecord domain) {
        return TossPaymentRecordJpaEntity.create(
            domain.getPaymentId(),
            domain.getVersion(),
            domain.getPaymentKey(),
            domain.getType(),
            domain.getOrderId(),
            domain.getOrderName(),
            domain.getMId(),
            domain.getCurrency(),
            domain.getMethod(),
            domain.getTotalAmount(),
            domain.getBalanceAmount(),
            domain.getStatus(),
            domain.getRequestedAt(),
            domain.getApprovedAt(),
            domain.isUseEscrow(),
            domain.getLastTransactionKey(),
            domain.getSuppliedAmount(),
            domain.getVat(),
            domain.isCultureExpense(),
            domain.getTaxFreeAmount(),
            domain.getTaxExemptionAmount(),
            domain.isPartialCancelable(),
            domain.getCardAmount(),
            domain.getCardIssuerCode(),
            domain.getCardAcquirerCode(),
            domain.getCardNumber(),
            domain.getCardInstallmentPlanMonths(),
            domain.getCardApproveNo(),
            domain.isCardUseCardPoint(),
            domain.getCardType(),
            domain.getCardOwnerType(),
            domain.getCardAcquireStatus(),
            domain.isCardInterestFree(),
            domain.getCardInterestPayer(),
            domain.getVirtualAccountType(),
            domain.getVirtualAccountNumber(),
            domain.getVirtualAccountBankCode(),
            domain.getVirtualAccountCustomerName(),
            domain.getVirtualAccountDueDate(),
            domain.getVirtualAccountRefundStatus(),
            domain.isVirtualAccountExpired(),
            domain.getVirtualAccountSettlementStatus(),
            domain.getMobilePhoneCustomerMobilePhone(),
            domain.getMobilePhoneSettlementStatus(),
            domain.getMobilePhoneReceiptUrl(),
            domain.getTransferBankCode(),
            domain.getTransferSettlementStatus(),
            domain.getEasyPayProvider(),
            domain.getEasyPayAmount(),
            domain.getEasyPayDiscountAmount(),
            domain.getReceiptUrl(),
            domain.getCheckoutUrl(),
            domain.getFailureCode(),
            domain.getFailureMessage(),
            domain.getCountry()
        );
    }
}

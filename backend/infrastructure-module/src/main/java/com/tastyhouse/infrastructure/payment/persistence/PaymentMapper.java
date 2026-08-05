package com.tastyhouse.infrastructure.payment.persistence;

import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.payment.model.Payment;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 결제 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class PaymentMapper {

    private PaymentMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static Payment toDomain(PaymentJpaEntity entity) {
        return Payment.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getOrderId(), OrderId::of),
            entity.getPaymentMethod(),
            entity.getPaymentStatus(),
            entity.getAmount(),
            entity.getPgProvider(),
            entity.getPgTid(),
            entity.getPgOrderId(),
            entity.getCardCompany(),
            entity.getCardNumber(),
            entity.getInstallmentMonths(),
            entity.getApprovedAt(),
            entity.getCancelledAt(),
            entity.getCancelReason(),
            entity.getReceiptUrl(),
            entity.getCreatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static PaymentJpaEntity toEntity(Payment domain) {
        return PaymentJpaEntity.create(
            IdMapping.raw(domain.getOrderId(), OrderId::value),
            domain.getPaymentMethod(),
            domain.getPaymentStatus(),
            domain.getAmount(),
            domain.getPgProvider(),
            domain.getPgTid(),
            domain.getPgOrderId(),
            domain.getCardCompany(),
            domain.getCardNumber(),
            domain.getInstallmentMonths(),
            domain.getApprovedAt(),
            domain.getCancelledAt(),
            domain.getCancelReason(),
            domain.getReceiptUrl()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(PaymentJpaEntity entity, Payment domain) {
        entity.applyChanges(
            domain.getPaymentStatus(),
            domain.getPgProvider(),
            domain.getPgTid(),
            domain.getPgOrderId(),
            domain.getCardCompany(),
            domain.getCardNumber(),
            domain.getInstallmentMonths(),
            domain.getApprovedAt(),
            domain.getCancelledAt(),
            domain.getCancelReason(),
            domain.getReceiptUrl()
        );
    }
}

package com.tastyhouse.infrastructure.payment.persistence;

import com.tastyhouse.domain.payment.model.PaymentRefund;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 결제 환불 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class PaymentRefundMapper {

    private PaymentRefundMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static PaymentRefund toDomain(PaymentRefundJpaEntity entity) {
        return PaymentRefund.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getPaymentId(), PaymentId::of),
            entity.getRefundAmount(),
            entity.getRefundReason(),
            entity.getRefundStatus(),
            entity.getPgRefundId(),
            entity.getRefundedAt(),
            entity.getCreatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static PaymentRefundJpaEntity toEntity(PaymentRefund domain) {
        return PaymentRefundJpaEntity.create(
            IdMapping.raw(domain.getPaymentId(), PaymentId::value),
            domain.getRefundAmount(),
            domain.getRefundReason(),
            domain.getRefundStatus(),
            domain.getPgRefundId(),
            domain.getRefundedAt()
        );
    }
}

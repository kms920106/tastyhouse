package com.tastyhouse.infrastructure.coupon.persistence;

import com.tastyhouse.domain.coupon.domain.model.Coupon;

/**
 * 쿠폰 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class CouponMapper {

    private CouponMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static Coupon toDomain(CouponJpaEntity entity) {
        return Coupon.reconstitute(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            entity.getDiscountType(),
            entity.getDiscountAmount(),
            entity.getMaxDiscountAmount(),
            entity.getMinOrderAmount(),
            entity.getMaxDiscountCount(),
            entity.getIssueStartAt(),
            entity.getIssueEndAt(),
            entity.getUseStartAt(),
            entity.getUseEndAt(),
            entity.isVisible(),
            entity.isDeleted(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static CouponJpaEntity toEntity(Coupon domain) {
        return CouponJpaEntity.create(
            domain.getName(),
            domain.getDescription(),
            domain.getDiscountType(),
            domain.getDiscountAmount(),
            domain.getMaxDiscountAmount(),
            domain.getMinOrderAmount(),
            domain.getMaxDiscountCount(),
            domain.getIssueStartAt(),
            domain.getIssueEndAt(),
            domain.getUseStartAt(),
            domain.getUseEndAt(),
            domain.isVisible(),
            domain.isDeleted()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(CouponJpaEntity entity, Coupon domain) {
        entity.applyChanges(
            domain.getName(),
            domain.getDescription(),
            domain.getDiscountType(),
            domain.getDiscountAmount(),
            domain.getMaxDiscountAmount(),
            domain.getMinOrderAmount(),
            domain.getMaxDiscountCount(),
            domain.getIssueStartAt(),
            domain.getIssueEndAt(),
            domain.getUseStartAt(),
            domain.getUseEndAt(),
            domain.isVisible(),
            domain.isDeleted()
        );
    }
}

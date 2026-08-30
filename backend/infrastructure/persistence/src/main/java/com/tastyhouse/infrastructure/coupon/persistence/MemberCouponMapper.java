package com.tastyhouse.infrastructure.coupon.persistence;

import com.tastyhouse.domain.coupon.model.MemberCoupon;
import com.tastyhouse.domain.coupon.vo.CouponId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 회원 쿠폰 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class MemberCouponMapper {

    private MemberCouponMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static MemberCoupon toDomain(MemberCouponJpaEntity entity) {
        return MemberCoupon.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getMemberId(), MemberId::of),
            IdMapping.vo(entity.getCouponId(), CouponId::of),
            entity.isUsed(),
            entity.getUsedAt(),
            entity.getExpiredAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static MemberCouponJpaEntity toEntity(MemberCoupon domain) {
        return MemberCouponJpaEntity.create(
            IdMapping.raw(domain.getMemberId(), MemberId::value),
            IdMapping.raw(domain.getCouponId(), CouponId::value),
            domain.isUsed(),
            domain.getUsedAt(),
            domain.getExpiredAt()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(MemberCouponJpaEntity entity, MemberCoupon domain) {
        entity.applyChanges(
            domain.isUsed(),
            domain.getUsedAt()
        );
    }
}

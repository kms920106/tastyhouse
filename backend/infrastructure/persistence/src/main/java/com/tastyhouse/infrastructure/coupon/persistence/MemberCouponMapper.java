package com.tastyhouse.infrastructure.coupon.persistence;

import com.tastyhouse.domain.coupon.model.MemberCoupon;
import com.tastyhouse.domain.coupon.vo.CouponId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class MemberCouponMapper {
    private MemberCouponMapper() {
    }

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

    static MemberCouponJpaEntity toEntity(MemberCoupon domain) {
        return MemberCouponJpaEntity.create(
            IdMapping.raw(domain.getMemberId(), MemberId::value),
            IdMapping.raw(domain.getCouponId(), CouponId::value),
            domain.isUsed(),
            domain.getUsedAt(),
            domain.getExpiredAt()
        );
    }

    static void applyChanges(MemberCouponJpaEntity entity, MemberCoupon domain) {
        entity.applyChanges(
            domain.isUsed(),
            domain.getUsedAt()
        );
    }
}

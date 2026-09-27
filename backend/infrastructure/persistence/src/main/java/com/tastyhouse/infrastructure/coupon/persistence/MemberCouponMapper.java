package com.tastyhouse.infrastructure.coupon.persistence;

import com.tastyhouse.application.coupon.port.out.write.MemberCouponState;

final class MemberCouponMapper {
    private MemberCouponMapper() {
    }

    static MemberCouponState toState(MemberCouponJpaEntity entity) {
        return new MemberCouponState(
            entity.getId(),
            entity.getMemberId(),
            entity.getCouponId(),
            entity.isUsed(),
            entity.getUsedAt(),
            entity.getExpiredAt()
        );
    }

    static MemberCouponJpaEntity toEntity(MemberCouponState state) {
        return MemberCouponJpaEntity.create(
            state.memberId(),
            state.couponId(),
            state.used(),
            state.usedAt(),
            state.expiredAt()
        );
    }

    static void applyChanges(MemberCouponJpaEntity entity, MemberCouponState state) {
        entity.applyChanges(
            state.used(),
            state.usedAt()
        );
    }
}

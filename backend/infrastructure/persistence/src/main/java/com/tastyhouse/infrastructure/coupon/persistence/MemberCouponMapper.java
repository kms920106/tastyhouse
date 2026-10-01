package com.tastyhouse.infrastructure.coupon.persistence;

import com.tastyhouse.domain.coupon.model.MemberCoupon;
import com.tastyhouse.domain.coupon.vo.CouponId;
import com.tastyhouse.domain.member.vo.MemberId;

final class MemberCouponMapper {

    private MemberCouponMapper() {
    }

    static MemberCoupon toDomain(MemberCouponJpaEntity entity) {
        return MemberCoupon.reconstitute(
            entity.getId(),
            entity.getMemberId() == null ? null : MemberId.of(entity.getMemberId()),
            entity.getCouponId() == null ? null : CouponId.of(entity.getCouponId()),
            entity.isUsed(),
            entity.getUsedAt(),
            entity.getExpiredAt()
        );
    }

    static MemberCouponJpaEntity toEntity(MemberCoupon memberCoupon) {
        return MemberCouponJpaEntity.create(
            memberCoupon.getMemberId() == null ? null : memberCoupon.getMemberId().value(),
            memberCoupon.getCouponId() == null ? null : memberCoupon.getCouponId().value(),
            memberCoupon.isUsed(),
            memberCoupon.getUsedAt(),
            memberCoupon.getExpiredAt()
        );
    }

    static void applyChanges(MemberCouponJpaEntity entity, MemberCoupon memberCoupon) {
        entity.applyChanges(
            memberCoupon.isUsed(),
            memberCoupon.getUsedAt()
        );
    }
}

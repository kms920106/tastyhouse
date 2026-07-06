package com.tastyhouse.core.domain.coupon.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.coupon.application.dto.result.MemberCouponResult;
import com.tastyhouse.core.domain.coupon.application.dto.result.QMemberCouponResult;
import com.tastyhouse.core.domain.coupon.domain.model.MemberCoupon;
import com.tastyhouse.core.domain.coupon.domain.repository.MemberCouponRepository;
import com.tastyhouse.core.domain.coupon.domain.vo.MemberCouponId;

import static com.tastyhouse.core.domain.coupon.domain.model.QCoupon.coupon;
import static com.tastyhouse.core.domain.coupon.domain.model.QMemberCoupon.memberCoupon;

@Repository
@RequiredArgsConstructor
public class MemberCouponRepositoryImpl implements MemberCouponRepository {

    private final MemberCouponJpaRepository memberCouponJpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<MemberCoupon> findById(MemberCouponId id) {
        return memberCouponJpaRepository.findById(id.value());
    }

    @Override
    public List<MemberCouponResult> findWithCouponByMemberId(Long memberId) {
        return queryFactory
            .select(new QMemberCouponResult(
                memberCoupon.id,
                coupon.id,
                coupon.name,
                coupon.description,
                coupon.discountType,
                coupon.discountAmount,
                coupon.maxDiscountAmount,
                coupon.minOrderAmount,
                coupon.useStartAt,
                coupon.useEndAt,
                memberCoupon.expiredAt,
                memberCoupon.used,
                memberCoupon.usedAt
            ))
            .from(memberCoupon)
            .join(coupon).on(coupon.id.eq(memberCoupon.couponId))
            .where(memberCoupon.memberId.eq(memberId))
            .fetch();
    }

    @Override
    public List<MemberCouponResult> findAvailableWithCouponByMemberId(Long memberId, LocalDateTime now) {
        return queryFactory
            .select(new QMemberCouponResult(
                memberCoupon.id,
                coupon.id,
                coupon.name,
                coupon.description,
                coupon.discountType,
                coupon.discountAmount,
                coupon.maxDiscountAmount,
                coupon.minOrderAmount,
                coupon.useStartAt,
                coupon.useEndAt,
                memberCoupon.expiredAt,
                memberCoupon.used,
                memberCoupon.usedAt
            ))
            .from(memberCoupon)
            .join(coupon).on(coupon.id.eq(memberCoupon.couponId))
            .where(
                memberCoupon.memberId.eq(memberId),
                memberCoupon.used.isFalse(),
                memberCoupon.expiredAt.gt(now)
            )
            .fetch();
    }

    @Override
    public MemberCoupon save(MemberCoupon memberCoupon) {
        return memberCouponJpaRepository.save(memberCoupon);
    }
}

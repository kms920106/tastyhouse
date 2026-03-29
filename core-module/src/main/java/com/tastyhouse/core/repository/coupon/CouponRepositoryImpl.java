package com.tastyhouse.core.repository.coupon;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.coupon.Coupon;
import com.tastyhouse.core.entity.coupon.MemberCoupon;
import com.tastyhouse.core.entity.coupon.QCoupon;
import com.tastyhouse.core.entity.coupon.QMemberCoupon;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepository {

    private final JPAQueryFactory queryFactory;
    private final CouponJpaRepository couponJpaRepository;
    private final MemberCouponJpaRepository memberCouponJpaRepository;

    @Override
    public List<Coupon> findActiveCoupons() {
        QCoupon coupon = QCoupon.coupon;

        return queryFactory
            .selectFrom(coupon)
            .where(coupon.isActive.isTrue())
            .fetch();
    }

    @Override
    public List<Coupon> findIssuableCoupons(LocalDateTime currentTime) {
        QCoupon coupon = QCoupon.coupon;

        return queryFactory
            .selectFrom(coupon)
            .where(
                coupon.isActive.isTrue(),
                coupon.issueStartAt.loe(currentTime),
                coupon.issueEndAt.goe(currentTime)
            )
            .fetch();
    }

    @Override
    public List<MemberCoupon> findMemberCouponsByMemberId(Long memberId) {
        QMemberCoupon memberCoupon = QMemberCoupon.memberCoupon;

        return queryFactory
            .selectFrom(memberCoupon)
            .where(memberCoupon.memberId.eq(memberId))
            .fetch();
    }

    @Override
    public List<MemberCoupon> findMemberCouponsByMemberIdAndIsUsed(Long memberId, Boolean isUsed) {
        QMemberCoupon memberCoupon = QMemberCoupon.memberCoupon;

        return queryFactory
            .selectFrom(memberCoupon)
            .where(
                memberCoupon.memberId.eq(memberId),
                memberCoupon.isUsed.eq(isUsed)
            )
            .fetch();
    }

    @Override
    public List<MemberCoupon> findAvailableMemberCouponsByMemberId(Long memberId, LocalDateTime currentTime) {
        QMemberCoupon memberCoupon = QMemberCoupon.memberCoupon;

        return queryFactory
            .selectFrom(memberCoupon)
            .where(
                memberCoupon.memberId.eq(memberId),
                memberCoupon.isUsed.isFalse(),
                memberCoupon.expiredAt.gt(currentTime)
            )
            .fetch();
    }

    @Override
    public Optional<MemberCoupon> findMemberCouponByMemberIdAndCouponId(Long memberId, Long couponId) {
        QMemberCoupon memberCoupon = QMemberCoupon.memberCoupon;

        MemberCoupon result = queryFactory
            .selectFrom(memberCoupon)
            .where(
                memberCoupon.memberId.eq(memberId),
                memberCoupon.couponId.eq(couponId)
            )
            .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public boolean existsMemberCouponByMemberIdAndCouponId(Long memberId, Long couponId) {
        QMemberCoupon memberCoupon = QMemberCoupon.memberCoupon;

        Long count = queryFactory
            .select(memberCoupon.count())
            .from(memberCoupon)
            .where(
                memberCoupon.memberId.eq(memberId),
                memberCoupon.couponId.eq(couponId)
            )
            .fetchOne();

        return count != null && count > 0;
    }

    @Override
    public Optional<Coupon> findCouponById(Long couponId) {
        return couponJpaRepository.findById(couponId);
    }

    @Override
    public Coupon saveCoupon(Coupon coupon) {
        return couponJpaRepository.save(coupon);
    }

    @Override
    public Optional<MemberCoupon> findMemberCouponById(Long memberCouponId) {
        return memberCouponJpaRepository.findById(memberCouponId);
    }

    @Override
    public MemberCoupon saveMemberCoupon(MemberCoupon memberCoupon) {
        return memberCouponJpaRepository.save(memberCoupon);
    }

    @Override
    public void deleteMemberCouponById(Long memberCouponId) {
        memberCouponJpaRepository.deleteById(memberCouponId);
    }
}

package com.tastyhouse.core.repository.coupon;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.coupon.MemberCoupon;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.tastyhouse.core.entity.coupon.QMemberCoupon.memberCoupon;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<MemberCoupon> findMemberCouponsByMemberId(Long memberId) {
        return queryFactory
            .selectFrom(memberCoupon)
            .where(memberCoupon.memberId.eq(memberId))
            .fetch();
    }

    @Override
    public List<MemberCoupon> findAvailableMemberCouponsByMemberId(Long memberId, LocalDateTime currentTime) {
        return queryFactory
            .selectFrom(memberCoupon)
            .where(
                memberCoupon.memberId.eq(memberId),
                memberCoupon.isUsed.isFalse(),
                memberCoupon.expiredAt.gt(currentTime)
            )
            .fetch();
    }
}

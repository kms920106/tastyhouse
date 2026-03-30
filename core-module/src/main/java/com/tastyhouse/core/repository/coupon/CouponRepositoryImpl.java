package com.tastyhouse.core.repository.coupon;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.coupon.MemberCoupon;
import com.tastyhouse.core.entity.coupon.QMemberCoupon;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<MemberCoupon> findMemberCouponsByMemberId(Long memberId) {
        QMemberCoupon memberCoupon = QMemberCoupon.memberCoupon;

        return queryFactory
            .selectFrom(memberCoupon)
            .where(memberCoupon.memberId.eq(memberId))
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
}

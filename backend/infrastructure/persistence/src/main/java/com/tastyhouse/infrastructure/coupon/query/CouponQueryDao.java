package com.tastyhouse.infrastructure.coupon.query;

import com.tastyhouse.application.coupon.port.out.CouponManagementQueryPort;
import com.tastyhouse.application.coupon.port.out.CouponQueryPort;
import com.tastyhouse.application.coupon.port.out.CouponDetailResult;
import com.tastyhouse.application.coupon.port.out.CouponListItemResult;
import com.tastyhouse.application.coupon.port.out.CouponSearchCondition;
import com.tastyhouse.application.coupon.port.out.MemberCouponItemResult;
import com.tastyhouse.application.coupon.port.out.MemberCouponResult;
import com.querydsl.core.types.Projections;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.domain.coupon.model.DiscountType;
import com.tastyhouse.domain.coupon.vo.CouponId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

import static com.tastyhouse.infrastructure.coupon.persistence.QCouponJpaEntity.couponJpaEntity;
import static com.tastyhouse.infrastructure.coupon.persistence.QMemberCouponJpaEntity.memberCouponJpaEntity;

@Repository
public class CouponQueryDao implements CouponQueryPort, CouponManagementQueryPort {
    private final JPAQueryFactory queryFactory;

    public CouponQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public PageResult<CouponListItemResult> findAllCoupons(CouponSearchCondition condition, PageQuery pageQuery) {
        Long total = queryFactory
            .select(couponJpaEntity.id.count())
            .from(couponJpaEntity)
            .where(
                couponJpaEntity.deleted.isFalse(),
                nameContains(condition.name()),
                discountTypeEq(condition.discountType()),
                visibleEq(condition.visible())
            )
            .fetchOne();

        List<CouponListItemResult> content = queryFactory
            .select(Projections.constructor(CouponListItemResult.class,
                couponJpaEntity.id,
                couponJpaEntity.name,
                couponJpaEntity.discountType,
                couponJpaEntity.discountAmount,
                couponJpaEntity.maxDiscountAmount,
                couponJpaEntity.minOrderAmount,
                couponJpaEntity.maxDiscountCount,
                couponJpaEntity.issueStartAt,
                couponJpaEntity.issueEndAt,
                couponJpaEntity.useStartAt,
                couponJpaEntity.useEndAt,
                couponJpaEntity.visible
            ))
            .from(couponJpaEntity)
            .where(
                couponJpaEntity.deleted.isFalse(),
                nameContains(condition.name()),
                discountTypeEq(condition.discountType()),
                visibleEq(condition.visible())
            )
            .orderBy(couponJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(content, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    @Override
    public Optional<CouponDetailResult> findCouponDetailById(CouponId couponId) {
        CouponDetailResult detail = queryFactory
            .select(Projections.constructor(CouponDetailResult.class,
                couponJpaEntity.id,
                couponJpaEntity.name,
                couponJpaEntity.description,
                couponJpaEntity.discountType,
                couponJpaEntity.discountAmount,
                couponJpaEntity.maxDiscountAmount,
                couponJpaEntity.minOrderAmount,
                couponJpaEntity.maxDiscountCount,
                couponJpaEntity.issueStartAt,
                couponJpaEntity.issueEndAt,
                couponJpaEntity.useStartAt,
                couponJpaEntity.useEndAt,
                couponJpaEntity.visible,
                couponJpaEntity.createdAt,
                couponJpaEntity.updatedAt
            ))
            .from(couponJpaEntity)
            .where(couponJpaEntity.id.eq(couponId.value()), couponJpaEntity.deleted.isFalse())
            .fetchOne();

        return Optional.ofNullable(detail);
    }

    @Override
    public PageResult<MemberCouponItemResult> findIssuedMemberCoupons(CouponId couponId, PageQuery pageQuery) {
        Long total = queryFactory
            .select(memberCouponJpaEntity.id.count())
            .from(memberCouponJpaEntity)
            .where(memberCouponJpaEntity.couponId.eq(couponId.value()))
            .fetchOne();

        List<MemberCouponItemResult> content = queryFactory
            .select(Projections.constructor(MemberCouponItemResult.class,
                memberCouponJpaEntity.id,
                memberCouponJpaEntity.memberId,
                memberCouponJpaEntity.used,
                memberCouponJpaEntity.usedAt,
                memberCouponJpaEntity.expiredAt,
                memberCouponJpaEntity.createdAt
            ))
            .from(memberCouponJpaEntity)
            .where(memberCouponJpaEntity.couponId.eq(couponId.value()))
            .orderBy(memberCouponJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(content, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    @Override
    public List<MemberCouponResult> findMemberCoupons(Long memberId) {
        return selectMemberCoupons()
            .where(memberCouponJpaEntity.memberId.eq(memberId))
            .fetch();
    }

    @Override
    public List<MemberCouponResult> findAvailableMemberCoupons(Long memberId, LocalDateTime now) {
        return selectMemberCoupons()
            .where(
                memberCouponJpaEntity.memberId.eq(memberId),
                memberCouponJpaEntity.used.isFalse(),
                memberCouponJpaEntity.expiredAt.gt(now)
            )
            .fetch();
    }

    private JPAQuery<MemberCouponResult> selectMemberCoupons() {
        return queryFactory
            .select(Projections.constructor(MemberCouponResult.class,
                memberCouponJpaEntity.id,
                couponJpaEntity.id,
                couponJpaEntity.name,
                couponJpaEntity.description,
                couponJpaEntity.discountType,
                couponJpaEntity.discountAmount,
                couponJpaEntity.maxDiscountAmount,
                couponJpaEntity.minOrderAmount,
                couponJpaEntity.useStartAt,
                couponJpaEntity.useEndAt,
                memberCouponJpaEntity.expiredAt,
                memberCouponJpaEntity.used,
                memberCouponJpaEntity.usedAt
            ))
            .from(memberCouponJpaEntity)
            .join(couponJpaEntity).on(couponJpaEntity.id.eq(memberCouponJpaEntity.couponId));
    }

    private BooleanExpression nameContains(String name) {
        return StringUtils.hasText(name) ? couponJpaEntity.name.containsIgnoreCase(name) : null;
    }

    private BooleanExpression discountTypeEq(DiscountType discountType) {
        return discountType != null ? couponJpaEntity.discountType.eq(discountType) : null;
    }

    private BooleanExpression visibleEq(Boolean visible) {
        return visible != null ? couponJpaEntity.visible.eq(visible) : null;
    }
}

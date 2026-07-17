package com.tastyhouse.core.domain.coupon.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.core.domain.coupon.domain.model.Coupon;
import com.tastyhouse.core.domain.coupon.domain.model.DiscountType;
import com.tastyhouse.core.domain.coupon.domain.repository.CouponRepository;
import com.tastyhouse.core.domain.coupon.domain.vo.CouponId;
import com.tastyhouse.core.domain.coupon.application.dto.CouponSearchCondition;
import com.tastyhouse.core.domain.coupon.application.dto.result.CouponListItemResult;
import com.tastyhouse.core.domain.coupon.application.dto.result.QCouponListItemResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

import static com.tastyhouse.core.domain.coupon.domain.model.QCoupon.coupon;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepository {

    private final JPAQueryFactory queryFactory;
    private final CouponJpaRepository couponJpaRepository;

    @Override
    public Optional<Coupon> findById(CouponId id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(queryFactory
            .selectFrom(coupon)
            .where(coupon.id.eq(id.value()), coupon.deleted.isFalse())
            .fetchOne());
    }

    @Override
    public PageResult<CouponListItemResult> findAllCoupons(CouponSearchCondition condition, PageQuery pageQuery) {
        Long total = queryFactory
            .select(coupon.id.count())
            .from(coupon)
            .where(
                coupon.deleted.isFalse(),
                nameContains(condition.name()),
                discountTypeEq(condition.discountType()),
                visibleEq(condition.visible())
            )
            .fetchOne();

        List<CouponListItemResult> coupons = queryFactory
            .select(new QCouponListItemResult(
                coupon.id,
                coupon.name,
                coupon.discountType,
                coupon.discountAmount,
                coupon.maxDiscountAmount,
                coupon.minOrderAmount,
                coupon.maxDiscountCount,
                coupon.issueStartAt,
                coupon.issueEndAt,
                coupon.useStartAt,
                coupon.useEndAt,
                coupon.visible
            ))
            .from(coupon)
            .where(
                coupon.deleted.isFalse(),
                nameContains(condition.name()),
                discountTypeEq(condition.discountType()),
                visibleEq(condition.visible())
            )
            .orderBy(coupon.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(coupons, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    @Override
    public Coupon save(Coupon coupon) {
        return couponJpaRepository.save(coupon);
    }

    private BooleanExpression nameContains(String name) {
        return StringUtils.hasText(name) ? coupon.name.containsIgnoreCase(name) : null;
    }

    private BooleanExpression discountTypeEq(DiscountType discountType) {
        return discountType != null ? coupon.discountType.eq(discountType) : null;
    }

    private BooleanExpression visibleEq(Boolean visible) {
        return visible != null ? coupon.visible.eq(visible) : null;
    }
}

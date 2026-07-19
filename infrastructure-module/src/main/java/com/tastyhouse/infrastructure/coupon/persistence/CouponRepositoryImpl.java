package com.tastyhouse.infrastructure.coupon.persistence;

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

import static com.tastyhouse.infrastructure.coupon.persistence.QCouponJpaEntity.couponJpaEntity;

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
        CouponJpaEntity entity = queryFactory
            .selectFrom(couponJpaEntity)
            .where(couponJpaEntity.id.eq(id.value()), couponJpaEntity.deleted.isFalse())
            .fetchOne();
        return Optional.ofNullable(entity).map(CouponMapper::toDomain);
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

        List<CouponListItemResult> coupons = queryFactory
            .select(new QCouponListItemResult(
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

        return PageResult.of(coupons, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    @Override
    public Coupon save(Coupon coupon) {
        if (coupon.getId() == null) {
            CouponJpaEntity saved = couponJpaRepository.save(CouponMapper.toEntity(coupon));
            return CouponMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        CouponJpaEntity entity = couponJpaRepository.findById(coupon.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 쿠폰입니다: " + coupon.getId()));
        CouponMapper.applyChanges(entity, coupon);
        return CouponMapper.toDomain(entity);
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

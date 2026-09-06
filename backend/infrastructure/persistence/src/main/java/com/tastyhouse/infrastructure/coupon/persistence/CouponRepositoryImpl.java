package com.tastyhouse.infrastructure.coupon.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.coupon.model.Coupon;
import com.tastyhouse.domain.coupon.repository.CouponRepository;
import com.tastyhouse.domain.coupon.vo.CouponId;

import static com.tastyhouse.infrastructure.coupon.persistence.QCouponJpaEntity.couponJpaEntity;

@Repository
public class CouponRepositoryImpl implements CouponRepository {
    private final JPAQueryFactory queryFactory;
    private final CouponJpaRepository couponJpaRepository;

    public CouponRepositoryImpl(JPAQueryFactory queryFactory, CouponJpaRepository couponJpaRepository) {
        this.queryFactory = queryFactory;
        this.couponJpaRepository = couponJpaRepository;
    }

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
    public Coupon save(Coupon coupon) {
        if (coupon.getId() == null) {
            CouponJpaEntity saved = couponJpaRepository.save(CouponMapper.toEntity(coupon));
            return CouponMapper.toDomain(saved);
        }

        CouponJpaEntity entity = couponJpaRepository.findById(coupon.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 쿠폰입니다: " + coupon.getId()));
        CouponMapper.applyChanges(entity, coupon);
        return CouponMapper.toDomain(entity);
    }
}

package com.tastyhouse.infrastructure.coupon.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.coupon.port.out.write.CouponState;
import com.tastyhouse.application.coupon.port.out.write.CouponStatePort;

import static com.tastyhouse.infrastructure.coupon.persistence.QCouponJpaEntity.couponJpaEntity;

@Repository
public class CouponStatePortImpl implements CouponStatePort {
    private final JPAQueryFactory queryFactory;
    private final CouponJpaRepository couponJpaRepository;

    public CouponStatePortImpl(JPAQueryFactory queryFactory, CouponJpaRepository couponJpaRepository) {
        this.queryFactory = queryFactory;
        this.couponJpaRepository = couponJpaRepository;
    }

    @Override
    public Optional<CouponState> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        CouponJpaEntity entity = queryFactory
            .selectFrom(couponJpaEntity)
            .where(couponJpaEntity.id.eq(id), couponJpaEntity.deleted.isFalse())
            .fetchOne();
        return Optional.ofNullable(entity).map(CouponMapper::toState);
    }

    @Override
    public CouponState save(CouponState state) {
        if (state.id() == null) {
            CouponJpaEntity saved = couponJpaRepository.save(CouponMapper.toEntity(state));
            return CouponMapper.toState(saved);
        }

        CouponJpaEntity entity = couponJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 쿠폰입니다: " + state.id()));
        CouponMapper.applyChanges(entity, state);
        return CouponMapper.toState(entity);
    }
}

package com.tastyhouse.infrastructure.coupon.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.coupon.domain.model.Coupon;
import com.tastyhouse.domain.coupon.domain.repository.CouponRepository;
import com.tastyhouse.domain.coupon.domain.vo.CouponId;

import static com.tastyhouse.infrastructure.coupon.persistence.QCouponJpaEntity.couponJpaEntity;

/**
 * 쿠폰 write 어댑터.
 *
 * <p>목록·상세 등 표현 목적 조회는 같은 모듈의 {@code CouponQueryDao}(query 패키지)로 이관했으므로,
 * 여기에는 단건 로드와 저장만 남는다.
 */
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

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        CouponJpaEntity entity = couponJpaRepository.findById(coupon.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 쿠폰입니다: " + coupon.getId()));
        CouponMapper.applyChanges(entity, coupon);
        return CouponMapper.toDomain(entity);
    }
}

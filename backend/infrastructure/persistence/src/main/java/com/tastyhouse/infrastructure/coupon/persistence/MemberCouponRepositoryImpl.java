package com.tastyhouse.infrastructure.coupon.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.coupon.model.MemberCoupon;
import com.tastyhouse.domain.coupon.repository.MemberCouponRepository;
import com.tastyhouse.domain.coupon.vo.CouponId;
import com.tastyhouse.domain.coupon.vo.MemberCouponId;
import com.tastyhouse.domain.member.vo.MemberId;

import static com.tastyhouse.infrastructure.coupon.persistence.QMemberCouponJpaEntity.memberCouponJpaEntity;

@Repository
public class MemberCouponRepositoryImpl implements MemberCouponRepository {
    private final MemberCouponJpaRepository memberCouponJpaRepository;
    private final JPAQueryFactory queryFactory;

    public MemberCouponRepositoryImpl(MemberCouponJpaRepository memberCouponJpaRepository, JPAQueryFactory queryFactory) {
        this.memberCouponJpaRepository = memberCouponJpaRepository;
        this.queryFactory = queryFactory;
    }

    @Override
    public Optional<MemberCoupon> findById(MemberCouponId id) {
        return memberCouponJpaRepository.findById(id.value()).map(MemberCouponMapper::toDomain);
    }

    @Override
    public boolean existsByMemberIdAndCouponId(MemberId memberId, CouponId couponId) {
        Integer found = queryFactory
            .selectOne()
            .from(memberCouponJpaEntity)
            .where(
                memberCouponJpaEntity.memberId.eq(memberId.value()),
                memberCouponJpaEntity.couponId.eq(couponId.value())
            )
            .fetchFirst();
        return found != null;
    }

    @Override
    public MemberCoupon save(MemberCoupon memberCoupon) {
        if (memberCoupon.getId() == null) {
            MemberCouponJpaEntity saved = memberCouponJpaRepository.save(MemberCouponMapper.toEntity(memberCoupon));
            return MemberCouponMapper.toDomain(saved);
        }

        MemberCouponJpaEntity entity = memberCouponJpaRepository.findById(memberCoupon.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 회원 쿠폰입니다: " + memberCoupon.getId()));
        MemberCouponMapper.applyChanges(entity, memberCoupon);
        return MemberCouponMapper.toDomain(entity);
    }
}

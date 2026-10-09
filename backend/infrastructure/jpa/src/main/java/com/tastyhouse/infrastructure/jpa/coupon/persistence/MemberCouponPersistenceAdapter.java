package com.tastyhouse.infrastructure.jpa.coupon.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.coupon.model.MemberCoupon;
import com.tastyhouse.domain.coupon.vo.CouponId;
import com.tastyhouse.domain.coupon.vo.MemberCouponId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.coupon.port.out.write.MemberCouponLoadPort;
import com.tastyhouse.application.coupon.port.out.write.MemberCouponSavePort;

import static com.tastyhouse.infrastructure.jpa.coupon.persistence.QMemberCouponJpaEntity.memberCouponJpaEntity;

@Repository
class MemberCouponPersistenceAdapter implements MemberCouponLoadPort, MemberCouponSavePort {

    private final MemberCouponJpaRepository memberCouponJpaRepository;
    private final JPAQueryFactory queryFactory;

    public MemberCouponPersistenceAdapter(MemberCouponJpaRepository memberCouponJpaRepository, JPAQueryFactory queryFactory) {
        this.memberCouponJpaRepository = memberCouponJpaRepository;
        this.queryFactory = queryFactory;
    }

    @Override
    public Optional<MemberCoupon> findById(MemberCouponId id) {
        return memberCouponJpaRepository.findById(id.value()).map(MemberCouponMapper::toDomain);
    }

    @Override
    public boolean existsByMemberIdAndCouponId(MemberId memberId, CouponId couponId) {
        return queryFactory
            .selectOne()
            .from(memberCouponJpaEntity)
            .where(
                memberCouponJpaEntity.memberId.eq(memberId.value()),
                memberCouponJpaEntity.couponId.eq(couponId.value())
            )
            .fetchFirst() != null;
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

package com.tastyhouse.infrastructure.coupon.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.coupon.port.out.write.MemberCouponState;
import com.tastyhouse.application.coupon.port.out.write.MemberCouponStatePort;

import static com.tastyhouse.infrastructure.coupon.persistence.QMemberCouponJpaEntity.memberCouponJpaEntity;

@Repository
public class MemberCouponStatePortImpl implements MemberCouponStatePort {
    private final MemberCouponJpaRepository memberCouponJpaRepository;
    private final JPAQueryFactory queryFactory;

    public MemberCouponStatePortImpl(MemberCouponJpaRepository memberCouponJpaRepository, JPAQueryFactory queryFactory) {
        this.memberCouponJpaRepository = memberCouponJpaRepository;
        this.queryFactory = queryFactory;
    }

    @Override
    public Optional<MemberCouponState> findById(Long id) {
        return memberCouponJpaRepository.findById(id).map(MemberCouponMapper::toState);
    }

    @Override
    public boolean existsByMemberIdAndCouponId(Long memberId, Long couponId) {
        Integer found = queryFactory
            .selectOne()
            .from(memberCouponJpaEntity)
            .where(
                memberCouponJpaEntity.memberId.eq(memberId),
                memberCouponJpaEntity.couponId.eq(couponId)
            )
            .fetchFirst();
        return found != null;
    }

    @Override
    public MemberCouponState save(MemberCouponState state) {
        if (state.id() == null) {
            MemberCouponJpaEntity saved = memberCouponJpaRepository.save(MemberCouponMapper.toEntity(state));
            return MemberCouponMapper.toState(saved);
        }

        MemberCouponJpaEntity entity = memberCouponJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 회원 쿠폰입니다: " + state.id()));
        MemberCouponMapper.applyChanges(entity, state);
        return MemberCouponMapper.toState(entity);
    }
}

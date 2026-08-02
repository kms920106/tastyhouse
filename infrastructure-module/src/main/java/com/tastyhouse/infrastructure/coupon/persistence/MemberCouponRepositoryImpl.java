package com.tastyhouse.infrastructure.coupon.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.coupon.domain.model.MemberCoupon;
import com.tastyhouse.domain.coupon.domain.repository.MemberCouponRepository;
import com.tastyhouse.domain.coupon.domain.vo.CouponId;
import com.tastyhouse.domain.coupon.domain.vo.MemberCouponId;
import com.tastyhouse.domain.member.domain.vo.MemberId;

import static com.tastyhouse.infrastructure.coupon.persistence.QMemberCouponJpaEntity.memberCouponJpaEntity;

/**
 * 회원 쿠폰 write 어댑터.
 *
 * <p>내 쿠폰함·발급 현황 등 표현 목적 조회는 같은 모듈의 {@code CouponQueryDao}(query 패키지)로
 * 이관했으므로, 여기에는 단건 로드·중복 발급 검증·저장만 남는다.
 */
@Repository
@RequiredArgsConstructor
public class MemberCouponRepositoryImpl implements MemberCouponRepository {

    private final MemberCouponJpaRepository memberCouponJpaRepository;
    private final JPAQueryFactory queryFactory;

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
                memberCouponJpaEntity.memberId.eq(memberId),
                memberCouponJpaEntity.couponId.eq(couponId)
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

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        MemberCouponJpaEntity entity = memberCouponJpaRepository.findById(memberCoupon.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 회원 쿠폰입니다: " + memberCoupon.getId()));
        MemberCouponMapper.applyChanges(entity, memberCoupon);
        return MemberCouponMapper.toDomain(entity);
    }
}

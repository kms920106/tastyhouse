package com.tastyhouse.infrastructure.coupon.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.coupon.domain.model.MemberCoupon;
import com.tastyhouse.core.domain.coupon.domain.repository.MemberCouponRepository;
import com.tastyhouse.core.domain.coupon.domain.vo.CouponId;
import com.tastyhouse.core.domain.coupon.domain.vo.MemberCouponId;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.coupon.application.dto.result.MemberCouponItemResult;
import com.tastyhouse.core.domain.coupon.application.dto.result.MemberCouponResult;
import com.tastyhouse.core.domain.coupon.application.dto.result.QMemberCouponItemResult;
import com.tastyhouse.core.domain.coupon.application.dto.result.QMemberCouponResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

import static com.tastyhouse.infrastructure.coupon.persistence.QCouponJpaEntity.couponJpaEntity;
import static com.tastyhouse.infrastructure.coupon.persistence.QMemberCouponJpaEntity.memberCouponJpaEntity;

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
    public List<MemberCouponResult> findWithCouponByMemberId(MemberId memberId) {
        return queryFactory
            .select(new QMemberCouponResult(
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
            .join(couponJpaEntity).on(couponJpaEntity.id.eq(memberCouponJpaEntity.couponId))
            .where(memberCouponJpaEntity.memberId.eq(memberId))
            .fetch();
    }

    @Override
    public List<MemberCouponResult> findAvailableWithCouponByMemberId(MemberId memberId, LocalDateTime now) {
        return queryFactory
            .select(new QMemberCouponResult(
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
            .join(couponJpaEntity).on(couponJpaEntity.id.eq(memberCouponJpaEntity.couponId))
            .where(
                memberCouponJpaEntity.memberId.eq(memberId),
                memberCouponJpaEntity.used.isFalse(),
                memberCouponJpaEntity.expiredAt.gt(now)
            )
            .fetch();
    }

    @Override
    public PageResult<MemberCouponItemResult> findByCouponId(CouponId couponId, PageQuery pageQuery) {
        Long total = queryFactory
            .select(memberCouponJpaEntity.id.count())
            .from(memberCouponJpaEntity)
            .where(memberCouponJpaEntity.couponId.eq(couponId.value()))
            .fetchOne();

        List<MemberCouponItemResult> items = queryFactory
            .select(new QMemberCouponItemResult(
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

        return PageResult.of(items, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    @Override
    public boolean existsByMemberIdAndCouponId(MemberId memberId, CouponId couponId) {
        Integer found = queryFactory
            .selectOne()
            .from(memberCouponJpaEntity)
            .where(
                memberCouponJpaEntity.memberId.eq(memberId),
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

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        MemberCouponJpaEntity entity = memberCouponJpaRepository.findById(memberCoupon.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 회원 쿠폰입니다: " + memberCoupon.getId()));
        MemberCouponMapper.applyChanges(entity, memberCoupon);
        return MemberCouponMapper.toDomain(entity);
    }
}

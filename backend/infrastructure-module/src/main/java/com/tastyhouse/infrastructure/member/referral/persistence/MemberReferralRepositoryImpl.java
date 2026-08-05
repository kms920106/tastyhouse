package com.tastyhouse.infrastructure.member.referral.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.referral.domain.model.MemberReferral;
import com.tastyhouse.domain.member.referral.domain.repository.MemberReferralRepository;
import com.tastyhouse.domain.member.referral.domain.vo.ReferralId;
import com.tastyhouse.domain.member.vo.MemberId;

import static com.tastyhouse.infrastructure.member.referral.persistence.QMemberReferralJpaEntity.memberReferralJpaEntity;

@Repository
public class MemberReferralRepositoryImpl implements MemberReferralRepository {

    private final JPAQueryFactory queryFactory;
    private final MemberReferralJpaRepository memberReferralJpaRepository;

    public MemberReferralRepositoryImpl(JPAQueryFactory queryFactory, MemberReferralJpaRepository memberReferralJpaRepository) {
        this.queryFactory = queryFactory;
        this.memberReferralJpaRepository = memberReferralJpaRepository;
    }

    @Override
    public boolean existsByRefereeId(MemberId refereeId) {
        return queryFactory
            .selectOne()
            .from(memberReferralJpaEntity)
            .where(memberReferralJpaEntity.refereeId.eq(refereeId))
            .fetchFirst() != null;
    }

    @Override
    public Optional<MemberReferral> findById(ReferralId id) {
        return memberReferralJpaRepository.findById(id.value())
            .map(MemberReferralMapper::toDomain);
    }

    @Override
    public MemberReferral save(MemberReferral referral) {
        if (referral.getId() == null) {
            MemberReferralJpaEntity saved = memberReferralJpaRepository.save(MemberReferralMapper.toEntity(referral));
            return MemberReferralMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        MemberReferralJpaEntity entity = memberReferralJpaRepository.findById(referral.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 추천입니다: " + referral.getId()));
        MemberReferralMapper.applyChanges(entity, referral);
        return MemberReferralMapper.toDomain(entity);
    }
}

package com.tastyhouse.infrastructure.jpa.member.referral.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.referral.model.MemberReferral;
import com.tastyhouse.domain.member.referral.vo.ReferralId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.referral.port.out.write.MemberReferralLoadPort;
import com.tastyhouse.application.member.referral.port.out.write.MemberReferralSavePort;

import static com.tastyhouse.infrastructure.jpa.member.referral.persistence.QMemberReferralJpaEntity.memberReferralJpaEntity;

@Repository
class MemberReferralPersistenceAdapter implements MemberReferralLoadPort, MemberReferralSavePort {

    private final JPAQueryFactory queryFactory;
    private final MemberReferralJpaRepository memberReferralJpaRepository;

    public MemberReferralPersistenceAdapter(JPAQueryFactory queryFactory, MemberReferralJpaRepository memberReferralJpaRepository) {
        this.queryFactory = queryFactory;
        this.memberReferralJpaRepository = memberReferralJpaRepository;
    }

    @Override
    public boolean existsByRefereeId(MemberId refereeId) {
        return queryFactory
            .selectOne()
            .from(memberReferralJpaEntity)
            .where(memberReferralJpaEntity.refereeId.eq(refereeId.value()))
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

        MemberReferralJpaEntity entity = memberReferralJpaRepository.findById(referral.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 추천입니다: " + referral.getId()));
        MemberReferralMapper.applyChanges(entity, referral);
        return MemberReferralMapper.toDomain(entity);
    }
}

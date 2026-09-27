package com.tastyhouse.infrastructure.member.referral.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.member.referral.port.out.write.MemberReferralState;
import com.tastyhouse.application.member.referral.port.out.write.MemberReferralStatePort;

import static com.tastyhouse.infrastructure.member.referral.persistence.QMemberReferralJpaEntity.memberReferralJpaEntity;

@Repository
public class MemberReferralStatePortImpl implements MemberReferralStatePort {
    private final JPAQueryFactory queryFactory;
    private final MemberReferralJpaRepository memberReferralJpaRepository;

    public MemberReferralStatePortImpl(JPAQueryFactory queryFactory, MemberReferralJpaRepository memberReferralJpaRepository) {
        this.queryFactory = queryFactory;
        this.memberReferralJpaRepository = memberReferralJpaRepository;
    }

    @Override
    public boolean existsByRefereeId(Long refereeId) {
        return queryFactory
            .selectOne()
            .from(memberReferralJpaEntity)
            .where(memberReferralJpaEntity.refereeId.eq(refereeId))
            .fetchFirst() != null;
    }

    @Override
    public Optional<MemberReferralState> findById(Long id) {
        return memberReferralJpaRepository.findById(id)
            .map(MemberReferralMapper::toState);
    }

    @Override
    public MemberReferralState save(MemberReferralState state) {
        if (state.id() == null) {
            MemberReferralJpaEntity saved = memberReferralJpaRepository.save(MemberReferralMapper.toEntity(state));
            return MemberReferralMapper.toState(saved);
        }

        MemberReferralJpaEntity entity = memberReferralJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 추천입니다: " + state.id()));
        MemberReferralMapper.applyChanges(entity, state);
        return MemberReferralMapper.toState(entity);
    }
}

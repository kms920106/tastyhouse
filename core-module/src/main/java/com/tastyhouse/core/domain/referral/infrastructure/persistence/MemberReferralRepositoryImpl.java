package com.tastyhouse.core.domain.referral.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.referral.domain.model.MemberReferral;
import com.tastyhouse.core.domain.referral.domain.repository.MemberReferralRepository;
import com.tastyhouse.core.domain.referral.domain.vo.ReferralId;

import static com.tastyhouse.core.domain.referral.domain.model.QMemberReferral.memberReferral;

@Repository
@RequiredArgsConstructor
public class MemberReferralRepositoryImpl implements MemberReferralRepository {

    private final JPAQueryFactory queryFactory;
    private final MemberReferralJpaRepository memberReferralJpaRepository;

    @Override
    public boolean existsByRefereeId(MemberId refereeId) {
        return queryFactory
            .selectOne()
            .from(memberReferral)
            .where(memberReferral.refereeId.eq(refereeId))
            .fetchFirst() != null;
    }

    @Override
    public List<MemberReferral> findByReferrerId(MemberId referrerId) {
        return queryFactory
            .selectFrom(memberReferral)
            .where(memberReferral.referrerId.eq(referrerId))
            .fetch();
    }

    @Override
    public Optional<MemberReferral> findById(ReferralId id) {
        return memberReferralJpaRepository.findById(id.value());
    }

    @Override
    public MemberReferral save(MemberReferral referral) {
        return memberReferralJpaRepository.save(referral);
    }
}

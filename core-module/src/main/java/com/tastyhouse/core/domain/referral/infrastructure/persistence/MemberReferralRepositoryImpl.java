package com.tastyhouse.core.domain.referral.infrastructure.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.domain.referral.domain.model.MemberReferral;
import com.tastyhouse.core.domain.referral.domain.model.QMemberReferral;
import com.tastyhouse.core.domain.referral.domain.model.ReferralStatus;
import com.tastyhouse.core.domain.referral.domain.repository.MemberReferralRepository;
import com.tastyhouse.core.domain.referral.domain.vo.ReferralId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.tastyhouse.core.domain.referral.domain.model.QMemberReferral.memberReferral;

@Repository
@RequiredArgsConstructor
public class MemberReferralRepositoryImpl implements MemberReferralRepository {

    private final JPAQueryFactory queryFactory;
    private final MemberReferralJpaRepository memberReferralJpaRepository;

    @Override
    public boolean existsByRefereeId(Long refereeId) {
        return queryFactory
            .selectOne()
            .from(memberReferral)
            .where(memberReferral.refereeId.eq(refereeId))
            .fetchFirst() != null;
    }

    @Override
    public List<MemberReferral> findByReferrerId(Long referrerId) {
        return queryFactory
            .selectFrom(memberReferral)
            .where(memberReferral.referrerId.eq(referrerId))
            .fetch();
    }

    @Override
    public List<MemberReferral> findByReferrerIdAndStatus(Long referrerId, ReferralStatus status) {
        return queryFactory
            .selectFrom(memberReferral)
            .where(
                memberReferral.referrerId.eq(referrerId),
                memberReferral.status.eq(status)
            )
            .fetch();
    }

    @Override
    public Optional<MemberReferral> findByRefereeId(Long refereeId) {
        MemberReferral result = queryFactory
            .selectFrom(memberReferral)
            .where(memberReferral.refereeId.eq(refereeId))
            .fetchOne();
        return Optional.ofNullable(result);
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

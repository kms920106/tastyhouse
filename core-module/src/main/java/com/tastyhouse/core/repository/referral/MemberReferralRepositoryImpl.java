package com.tastyhouse.core.repository.referral;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.referral.MemberReferral;
import com.tastyhouse.core.entity.referral.QMemberReferral;
import com.tastyhouse.core.entity.referral.ReferralStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberReferralRepositoryImpl implements MemberReferralRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public boolean existsByRefereeId(Long refereeId) {
        QMemberReferral referral = QMemberReferral.memberReferral;
        return queryFactory
            .selectOne()
            .from(referral)
            .where(referral.refereeId.eq(refereeId))
            .fetchFirst() != null;
    }

    @Override
    public List<MemberReferral> findByReferrerId(Long referrerId) {
        QMemberReferral referral = QMemberReferral.memberReferral;
        return queryFactory
            .selectFrom(referral)
            .where(referral.referrerId.eq(referrerId))
            .fetch();
    }

    @Override
    public List<MemberReferral> findByReferrerIdAndStatus(Long referrerId, ReferralStatus status) {
        QMemberReferral referral = QMemberReferral.memberReferral;
        return queryFactory
            .selectFrom(referral)
            .where(
                referral.referrerId.eq(referrerId),
                referral.status.eq(status)
            )
            .fetch();
    }

    @Override
    public Optional<MemberReferral> findByRefereeId(Long refereeId) {
        QMemberReferral referral = QMemberReferral.memberReferral;
        MemberReferral result = queryFactory
            .selectFrom(referral)
            .where(referral.refereeId.eq(refereeId))
            .fetchOne();
        return Optional.ofNullable(result);
    }
}

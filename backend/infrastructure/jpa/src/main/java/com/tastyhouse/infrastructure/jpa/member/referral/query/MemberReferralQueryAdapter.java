package com.tastyhouse.infrastructure.jpa.member.referral.query;

import java.util.List;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.member.referral.port.out.MemberReferralQueryPort;
import com.tastyhouse.application.member.referral.port.out.MemberReferralResult;

import static com.tastyhouse.infrastructure.jpa.member.referral.persistence.QMemberReferralJpaEntity.memberReferralJpaEntity;

@Repository
class MemberReferralQueryAdapter implements MemberReferralQueryPort {

    private final JPAQueryFactory queryFactory;

    public MemberReferralQueryAdapter(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<MemberReferralResult> findByReferrerId(Long referrerId) {
        return queryFactory
            .select(Projections.constructor(MemberReferralResult.class,
                memberReferralJpaEntity.id,
                memberReferralJpaEntity.referrerId,
                memberReferralJpaEntity.refereeId,
                memberReferralJpaEntity.status.stringValue(),
                memberReferralJpaEntity.createdAt
            ))
            .from(memberReferralJpaEntity)
            .where(memberReferralJpaEntity.referrerId.eq(referrerId))
            .orderBy(memberReferralJpaEntity.createdAt.desc())
            .fetch();
    }
}

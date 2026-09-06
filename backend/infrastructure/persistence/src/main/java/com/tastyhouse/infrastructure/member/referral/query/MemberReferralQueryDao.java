package com.tastyhouse.infrastructure.member.referral.query;

import com.tastyhouse.application.member.referral.port.out.MemberReferralQueryPort;
import com.tastyhouse.application.member.referral.port.out.MemberReferralResult;
import com.querydsl.core.types.Projections;
import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.vo.MemberId;

import static com.tastyhouse.infrastructure.member.referral.persistence.QMemberReferralJpaEntity.memberReferralJpaEntity;

@Repository
public class MemberReferralQueryDao implements MemberReferralQueryPort {
    private final JPAQueryFactory queryFactory;

    public MemberReferralQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<MemberReferralResult> findByReferrerId(MemberId referrerId) {
        return queryFactory
            .select(Projections.constructor(MemberReferralResult.class,
                memberReferralJpaEntity.id,
                memberReferralJpaEntity.referrerId,
                memberReferralJpaEntity.refereeId,
                memberReferralJpaEntity.status,
                memberReferralJpaEntity.createdAt
            ))
            .from(memberReferralJpaEntity)
            .where(memberReferralJpaEntity.referrerId.eq(referrerId.value()))
            .orderBy(memberReferralJpaEntity.createdAt.desc())
            .fetch();
    }
}

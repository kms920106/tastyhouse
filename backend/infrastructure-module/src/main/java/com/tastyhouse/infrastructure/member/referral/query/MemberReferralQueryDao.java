package com.tastyhouse.infrastructure.member.referral.query;

import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.vo.MemberId;

import static com.tastyhouse.infrastructure.member.referral.persistence.QMemberReferralJpaEntity.memberReferralJpaEntity;

/**
 * 추천 관계 read 어댑터(CQRS query 측).
 *
 * <p>내 추천 목록 화면이 소비하는 표현 목적 조회를 JPA 엔티티에서 Result DTO로 직접 투영한다.
 * 소비 모듈(web-api)의 {@code ReferralQueryService}가 이 DAO를 주입하므로 api 모듈은 QueryDSL을
 * 알지 않는다.
 */
@Repository
public class MemberReferralQueryDao {

    private final JPAQueryFactory queryFactory;

    public MemberReferralQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 내가(referrerId) 추천한 회원 목록 — 최근 등록순.
     */
    public List<MemberReferralResult> findByReferrerId(MemberId referrerId) {
        return queryFactory
            .select(new QMemberReferralResult(
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

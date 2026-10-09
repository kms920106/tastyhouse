package com.tastyhouse.infrastructure.persistence.rank.persistence;

import java.time.LocalDate;
import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.rank.model.MemberReviewRank;
import com.tastyhouse.domain.rank.model.RankType;
import com.tastyhouse.application.rank.port.out.write.MemberReviewRankSavePort;

import static com.tastyhouse.infrastructure.persistence.rank.persistence.QMemberReviewRankJpaEntity.memberReviewRankJpaEntity;

@Repository
class MemberReviewRankPersistenceAdapter implements MemberReviewRankSavePort {

    private final JPAQueryFactory queryFactory;
    private final MemberReviewRankJpaRepository memberReviewRankJpaRepository;
    private final EntityManager entityManager;

    public MemberReviewRankPersistenceAdapter(
        JPAQueryFactory queryFactory,
        MemberReviewRankJpaRepository memberReviewRankJpaRepository,
        EntityManager entityManager
    ) {
        this.queryFactory = queryFactory;
        this.memberReviewRankJpaRepository = memberReviewRankJpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    public void saveAll(List<MemberReviewRank> ranks) {
        List<MemberReviewRankJpaEntity> entities = ranks.stream()
            .map(MemberReviewRankMapper::toEntity)
            .toList();
        memberReviewRankJpaRepository.saveAll(entities);
    }

    @Override
    public void deleteByRankTypeAndBaseDate(RankType rankType, LocalDate baseDate) {
        queryFactory
            .delete(memberReviewRankJpaEntity)
            .where(
                memberReviewRankJpaEntity.rankType.eq(rankType.name()),
                memberReviewRankJpaEntity.baseDate.eq(baseDate)
            )
            .execute();
        entityManager.flush();
        entityManager.clear();
    }
}

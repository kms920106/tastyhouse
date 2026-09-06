package com.tastyhouse.infrastructure.rank.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.rank.model.MemberReviewRank;
import com.tastyhouse.domain.rank.model.RankType;
import com.tastyhouse.domain.rank.repository.MemberReviewRankRepository;

import static com.tastyhouse.infrastructure.rank.persistence.QMemberReviewRankJpaEntity.memberReviewRankJpaEntity;

@Repository
public class MemberReviewRankRepositoryImpl implements MemberReviewRankRepository {
    private final JPAQueryFactory queryFactory;
    private final MemberReviewRankJpaRepository memberReviewRankJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public MemberReviewRankRepositoryImpl(JPAQueryFactory queryFactory, MemberReviewRankJpaRepository memberReviewRankJpaRepository) {
        this.queryFactory = queryFactory;
        this.memberReviewRankJpaRepository = memberReviewRankJpaRepository;
    }

    @Override
    public Optional<MemberReviewRank> findLatestByMemberIdAndRankType(MemberId memberId, RankType rankType) {
        MemberReviewRankJpaEntity entity = queryFactory
            .selectFrom(memberReviewRankJpaEntity)
            .where(
                memberReviewRankJpaEntity.memberId.eq(memberId.value()),
                memberReviewRankJpaEntity.rankType.eq(rankType)
            )
            .orderBy(memberReviewRankJpaEntity.baseDate.desc())
            .fetchFirst();
        return Optional.ofNullable(entity).map(MemberReviewRankMapper::toDomain);
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
                memberReviewRankJpaEntity.rankType.eq(rankType),
                memberReviewRankJpaEntity.baseDate.eq(baseDate)
            )
            .execute();
        entityManager.flush();
        entityManager.clear();
    }
}

package com.tastyhouse.infrastructure.rank.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.rank.port.out.write.MemberReviewRankState;
import com.tastyhouse.application.rank.port.out.write.MemberReviewRankStatePort;

import static com.tastyhouse.infrastructure.rank.persistence.QMemberReviewRankJpaEntity.memberReviewRankJpaEntity;

@Repository
public class MemberReviewRankStatePortImpl implements MemberReviewRankStatePort {
    private final JPAQueryFactory queryFactory;
    private final MemberReviewRankJpaRepository memberReviewRankJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public MemberReviewRankStatePortImpl(JPAQueryFactory queryFactory, MemberReviewRankJpaRepository memberReviewRankJpaRepository) {
        this.queryFactory = queryFactory;
        this.memberReviewRankJpaRepository = memberReviewRankJpaRepository;
    }

    @Override
    public Optional<MemberReviewRankState> findLatestByMemberIdAndRankType(Long memberId, String rankType) {
        MemberReviewRankJpaEntity entity = queryFactory
            .selectFrom(memberReviewRankJpaEntity)
            .where(
                memberReviewRankJpaEntity.memberId.eq(memberId),
                memberReviewRankJpaEntity.rankType.eq(rankType)
            )
            .orderBy(memberReviewRankJpaEntity.baseDate.desc())
            .fetchFirst();
        return Optional.ofNullable(entity).map(MemberReviewRankMapper::toState);
    }

    @Override
    public void saveAll(List<MemberReviewRankState> states) {
        List<MemberReviewRankJpaEntity> entities = states.stream()
            .map(MemberReviewRankMapper::toEntity)
            .toList();
        memberReviewRankJpaRepository.saveAll(entities);
    }

    @Override
    public void deleteByRankTypeAndBaseDate(String rankType, LocalDate baseDate) {
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

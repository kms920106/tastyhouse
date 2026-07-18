package com.tastyhouse.core.domain.rank.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.rank.domain.model.RankPeriod;
import com.tastyhouse.core.domain.rank.domain.repository.RankPeriodRepository;
import com.tastyhouse.core.domain.rank.domain.vo.RankPeriodId;
import com.tastyhouse.core.domain.rank.application.dto.result.RankPeriodResult;

import static com.tastyhouse.core.domain.rank.domain.model.QRankPeriod.rankPeriod;

@Repository
@RequiredArgsConstructor
public class RankPeriodRepositoryImpl implements RankPeriodRepository {

    private final JPAQueryFactory queryFactory;
    private final RankPeriodJpaRepository rankPeriodJpaRepository;

    @Override
    public RankPeriod save(RankPeriod rankPeriod) {
        return rankPeriodJpaRepository.save(rankPeriod);
    }

    @Override
    public Optional<RankPeriod> findById(RankPeriodId id) {
        return rankPeriodJpaRepository.findById(id.value());
    }

    @Override
    public List<RankPeriodResult> findAllPeriods() {
        return queryFactory
            .select(Projections.constructor(RankPeriodResult.class,
                rankPeriod.id,
                rankPeriod.startAt,
                rankPeriod.endAt,
                rankPeriod.visible,
                rankPeriod.createdAt,
                rankPeriod.updatedAt
            ))
            .from(rankPeriod)
            .orderBy(rankPeriod.startAt.desc())
            .fetch();
    }

    @Override
    public Optional<RankPeriodResult> findPeriodById(RankPeriodId id) {
        RankPeriodResult result = queryFactory
            .select(Projections.constructor(RankPeriodResult.class,
                rankPeriod.id,
                rankPeriod.startAt,
                rankPeriod.endAt,
                rankPeriod.visible,
                rankPeriod.createdAt,
                rankPeriod.updatedAt
            ))
            .from(rankPeriod)
            .where(rankPeriod.id.eq(id.value()))
            .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public void delete(RankPeriod rankPeriod) {
        rankPeriodJpaRepository.delete(rankPeriod);
    }
}

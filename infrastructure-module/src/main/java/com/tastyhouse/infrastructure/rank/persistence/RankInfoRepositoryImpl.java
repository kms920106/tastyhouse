package com.tastyhouse.infrastructure.rank.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.rank.domain.repository.RankInfoRepository;
import com.tastyhouse.core.domain.rank.application.dto.result.RankDurationResult;
import com.tastyhouse.core.domain.rank.application.dto.result.RankPrizeResult;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.rank.persistence.QRankPeriodJpaEntity.rankPeriodJpaEntity;
import static com.tastyhouse.infrastructure.rank.persistence.QRankPrizeJpaEntity.rankPrizeJpaEntity;

@Repository
@RequiredArgsConstructor
public class RankInfoRepositoryImpl implements RankInfoRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<RankDurationResult> findActiveDuration() {
        RankDurationResult result = queryFactory
            .select(Projections.constructor(RankDurationResult.class,
                rankPeriodJpaEntity.startAt,
                rankPeriodJpaEntity.endAt
            ))
            .from(rankPeriodJpaEntity)
            .where(rankPeriodJpaEntity.visible.isTrue(), rankPeriodJpaEntity.deleted.isFalse())
            .orderBy(rankPeriodJpaEntity.startAt.desc())
            .limit(1)
            .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public List<RankPrizeResult> findActivePrizes() {
        return queryFactory
            .select(Projections.constructor(RankPrizeResult.class,
                rankPrizeJpaEntity.id,
                rankPrizeJpaEntity.prizeRank,
                rankPrizeJpaEntity.name,
                rankPrizeJpaEntity.brand,
                uploadedFileJpaEntity.filePath
            ))
            .from(rankPeriodJpaEntity)
            .innerJoin(rankPrizeJpaEntity).on(rankPrizeJpaEntity.rankId.eq(rankPeriodJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(rankPrizeJpaEntity.imageFileId.eq(uploadedFileJpaEntity.id))
            .where(
                rankPeriodJpaEntity.visible.isTrue(),
                rankPeriodJpaEntity.deleted.isFalse(),
                rankPrizeJpaEntity.deleted.isFalse()
            )
            .orderBy(rankPeriodJpaEntity.startAt.desc(), rankPrizeJpaEntity.prizeRank.asc())
            .fetch();
    }
}

package com.tastyhouse.core.domain.rank.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.rank.domain.repository.RankInfoRepository;
import com.tastyhouse.core.domain.rank.application.dto.result.RankDurationResult;
import com.tastyhouse.core.domain.rank.application.dto.result.RankPrizeResult;

import static com.tastyhouse.core.domain.file.domain.model.QUploadedFile.uploadedFile;
import static com.tastyhouse.core.domain.rank.domain.model.QRankPeriod.rankPeriod;
import static com.tastyhouse.core.domain.rank.domain.model.QRankPrize.rankPrize;

@Repository
@RequiredArgsConstructor
public class RankInfoRepositoryImpl implements RankInfoRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<RankDurationResult> findActiveDuration() {
        RankDurationResult result = queryFactory
            .select(Projections.constructor(RankDurationResult.class,
                rankPeriod.startAt,
                rankPeriod.endAt
            ))
            .from(rankPeriod)
            .where(rankPeriod.visible.isTrue())
            .orderBy(rankPeriod.startAt.desc())
            .limit(1)
            .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public List<RankPrizeResult> findActivePrizes() {
        return queryFactory
            .select(Projections.constructor(RankPrizeResult.class,
                rankPrize.id,
                rankPrize.prizeRank,
                rankPrize.name,
                rankPrize.brand,
                uploadedFile.filePath
            ))
            .from(rankPeriod)
            .innerJoin(rankPrize).on(rankPrize.rankId.eq(rankPeriod.id))
            .leftJoin(uploadedFile).on(rankPrize.imageFileId.eq(uploadedFile.id))
            .where(rankPeriod.visible.isTrue())
            .orderBy(rankPeriod.startAt.desc(), rankPrize.prizeRank.asc())
            .fetch();
    }
}

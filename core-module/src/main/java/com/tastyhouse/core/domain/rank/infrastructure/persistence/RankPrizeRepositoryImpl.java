package com.tastyhouse.core.domain.rank.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.rank.domain.model.RankPrize;
import com.tastyhouse.core.domain.rank.domain.repository.RankPrizeRepository;
import com.tastyhouse.core.domain.rank.domain.vo.RankPeriodId;
import com.tastyhouse.core.domain.rank.domain.vo.RankPrizeId;
import com.tastyhouse.core.domain.rank.application.dto.result.RankPrizeManagementResult;

import static com.tastyhouse.core.domain.file.domain.model.QUploadedFile.uploadedFile;
import static com.tastyhouse.core.domain.rank.domain.model.QRankPrize.rankPrize;

@Repository
@RequiredArgsConstructor
public class RankPrizeRepositoryImpl implements RankPrizeRepository {

    private final JPAQueryFactory queryFactory;
    private final RankPrizeJpaRepository rankPrizeJpaRepository;

    @Override
    public RankPrize save(RankPrize rankPrize) {
        return rankPrizeJpaRepository.save(rankPrize);
    }

    @Override
    public Optional<RankPrize> findById(RankPrizeId id) {
        return rankPrizeJpaRepository.findById(id.value());
    }

    @Override
    public List<RankPrizeManagementResult> findByPeriodId(RankPeriodId periodId) {
        return queryFactory
            .select(Projections.constructor(RankPrizeManagementResult.class,
                rankPrize.id,
                rankPrize.rankId,
                rankPrize.prizeRank,
                rankPrize.name,
                rankPrize.brand,
                rankPrize.imageFileId,
                uploadedFile.originalFilename,
                uploadedFile.filePath
            ))
            .from(rankPrize)
            .leftJoin(uploadedFile).on(uploadedFile.id.eq(rankPrize.imageFileId))
            .where(rankPrize.rankId.eq(periodId.value()))
            .orderBy(rankPrize.prizeRank.asc())
            .fetch();
    }

    @Override
    public Optional<RankPrizeManagementResult> findPrizeById(RankPrizeId id) {
        RankPrizeManagementResult result = queryFactory
            .select(Projections.constructor(RankPrizeManagementResult.class,
                rankPrize.id,
                rankPrize.rankId,
                rankPrize.prizeRank,
                rankPrize.name,
                rankPrize.brand,
                rankPrize.imageFileId,
                uploadedFile.originalFilename,
                uploadedFile.filePath
            ))
            .from(rankPrize)
            .leftJoin(uploadedFile).on(uploadedFile.id.eq(rankPrize.imageFileId))
            .where(rankPrize.id.eq(id.value()))
            .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public void delete(RankPrize rankPrize) {
        rankPrizeJpaRepository.delete(rankPrize);
    }
}

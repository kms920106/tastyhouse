package com.tastyhouse.core.repository.rank;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.rank.dto.RankDurationDto;
import com.tastyhouse.core.entity.rank.dto.RankPrizeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.tastyhouse.core.domain.file.domain.model.QUploadedFile.uploadedFile;
import static com.tastyhouse.core.entity.rank.QRank.rank;
import static com.tastyhouse.core.entity.rank.QRankPrize.rankPrize;

@Repository
@RequiredArgsConstructor
public class RankRepositoryImpl implements RankRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<RankDurationDto> findActiveDuration() {
        RankDurationDto result = queryFactory
            .select(Projections.constructor(RankDurationDto.class,
                rank.startAt,
                rank.endAt
            ))
            .from(rank)
            .where(rank.isActive.isTrue())
            .orderBy(rank.startAt.desc())
            .limit(1)
            .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public List<RankPrizeDto> findActivePrizes() {
        return queryFactory
            .select(Projections.constructor(RankPrizeDto.class,
                rankPrize.id,
                rankPrize.prizeRank,
                rankPrize.name,
                rankPrize.brand,
                uploadedFile.filePath
            ))
            .from(rank)
            .innerJoin(rankPrize).on(rankPrize.rankId.eq(rank.id))
            .leftJoin(uploadedFile).on(rankPrize.imageFileId.eq(uploadedFile.id))
            .where(rank.isActive.isTrue())
            .orderBy(rank.startAt.desc(), rankPrize.prizeRank.asc())
            .fetch();
    }
}

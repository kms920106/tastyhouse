package com.tastyhouse.infrastructure.rank.persistence;

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

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.rank.persistence.QRankPrizeJpaEntity.rankPrizeJpaEntity;

@Repository
@RequiredArgsConstructor
public class RankPrizeRepositoryImpl implements RankPrizeRepository {

    private final JPAQueryFactory queryFactory;
    private final RankPrizeJpaRepository rankPrizeJpaRepository;

    @Override
    public RankPrize save(RankPrize rankPrize) {
        if (rankPrize.getId() == null) {
            RankPrizeJpaEntity saved = rankPrizeJpaRepository.save(RankPrizeMapper.toEntity(rankPrize));
            return RankPrizeMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        RankPrizeJpaEntity entity = rankPrizeJpaRepository.findById(rankPrize.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 랭킹 경품입니다: " + rankPrize.getId()));
        RankPrizeMapper.applyChanges(entity, rankPrize);
        return RankPrizeMapper.toDomain(entity);
    }

    @Override
    public Optional<RankPrize> findById(RankPrizeId id) {
        RankPrizeJpaEntity entity = queryFactory
            .selectFrom(rankPrizeJpaEntity)
            .where(rankPrizeJpaEntity.id.eq(id.value()), rankPrizeJpaEntity.deleted.isFalse())
            .fetchOne();
        return Optional.ofNullable(entity).map(RankPrizeMapper::toDomain);
    }

    @Override
    public List<RankPrizeManagementResult> findByPeriodId(RankPeriodId periodId) {
        return queryFactory
            .select(Projections.constructor(RankPrizeManagementResult.class,
                rankPrizeJpaEntity.id,
                rankPrizeJpaEntity.rankId,
                rankPrizeJpaEntity.prizeRank,
                rankPrizeJpaEntity.name,
                rankPrizeJpaEntity.brand,
                rankPrizeJpaEntity.imageFileId,
                uploadedFileJpaEntity.originalFilename,
                uploadedFileJpaEntity.filePath
            ))
            .from(rankPrizeJpaEntity)
            .leftJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(rankPrizeJpaEntity.imageFileId))
            .where(rankPrizeJpaEntity.rankId.eq(periodId.value()), rankPrizeJpaEntity.deleted.isFalse())
            .orderBy(rankPrizeJpaEntity.prizeRank.asc())
            .fetch();
    }

    @Override
    public Optional<RankPrizeManagementResult> findPrizeById(RankPrizeId id) {
        RankPrizeManagementResult result = queryFactory
            .select(Projections.constructor(RankPrizeManagementResult.class,
                rankPrizeJpaEntity.id,
                rankPrizeJpaEntity.rankId,
                rankPrizeJpaEntity.prizeRank,
                rankPrizeJpaEntity.name,
                rankPrizeJpaEntity.brand,
                rankPrizeJpaEntity.imageFileId,
                uploadedFileJpaEntity.originalFilename,
                uploadedFileJpaEntity.filePath
            ))
            .from(rankPrizeJpaEntity)
            .leftJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(rankPrizeJpaEntity.imageFileId))
            .where(rankPrizeJpaEntity.id.eq(id.value()), rankPrizeJpaEntity.deleted.isFalse())
            .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public void delete(RankPrize rankPrize) {
        // 하드 삭제 -> 소프트 삭제 전환: 필터 없는 순수 PK 조회(managed) 후 deleted 플래그만 갱신해 flush.
        RankPrizeJpaEntity entity = rankPrizeJpaRepository.findById(rankPrize.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 랭킹 경품입니다: " + rankPrize.getId()));
        entity.applyChanges(entity.getPrizeRank(), entity.getName(), entity.getBrand(), entity.getImageFileId(), true);
    }
}

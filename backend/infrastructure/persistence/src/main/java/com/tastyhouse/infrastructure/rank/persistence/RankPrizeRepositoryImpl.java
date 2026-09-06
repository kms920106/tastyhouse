package com.tastyhouse.infrastructure.rank.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.rank.model.RankPrize;
import com.tastyhouse.domain.rank.repository.RankPrizeRepository;
import com.tastyhouse.domain.rank.vo.RankPrizeId;

import static com.tastyhouse.infrastructure.rank.persistence.QRankPrizeJpaEntity.rankPrizeJpaEntity;

@Repository
public class RankPrizeRepositoryImpl implements RankPrizeRepository {
    private final JPAQueryFactory queryFactory;
    private final RankPrizeJpaRepository rankPrizeJpaRepository;

    public RankPrizeRepositoryImpl(JPAQueryFactory queryFactory, RankPrizeJpaRepository rankPrizeJpaRepository) {
        this.queryFactory = queryFactory;
        this.rankPrizeJpaRepository = rankPrizeJpaRepository;
    }

    @Override
    public RankPrize save(RankPrize rankPrize) {
        if (rankPrize.getId() == null) {
            RankPrizeJpaEntity saved = rankPrizeJpaRepository.save(RankPrizeMapper.toEntity(rankPrize));
            return RankPrizeMapper.toDomain(saved);
        }

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
    public void delete(RankPrize rankPrize) {
        RankPrizeJpaEntity entity = rankPrizeJpaRepository.findById(rankPrize.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 랭킹 경품입니다: " + rankPrize.getId()));
        entity.applyChanges(entity.getPrizeRank(), entity.getName(), entity.getBrand(), entity.getImageFileId(), true);
    }
}

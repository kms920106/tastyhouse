package com.tastyhouse.infrastructure.jpa.rank.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.rank.model.RankPrize;
import com.tastyhouse.domain.rank.vo.RankPrizeId;
import com.tastyhouse.application.rank.port.out.write.RankPrizeLoadPort;
import com.tastyhouse.application.rank.port.out.write.RankPrizeSavePort;

import static com.tastyhouse.infrastructure.jpa.rank.persistence.QRankPrizeJpaEntity.rankPrizeJpaEntity;

@Repository
class RankPrizePersistenceAdapter implements RankPrizeLoadPort, RankPrizeSavePort {

    private final JPAQueryFactory queryFactory;
    private final RankPrizeJpaRepository rankPrizeJpaRepository;

    public RankPrizePersistenceAdapter(JPAQueryFactory queryFactory, RankPrizeJpaRepository rankPrizeJpaRepository) {
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
        Long id = rankPrize.getId();
        RankPrizeJpaEntity entity = rankPrizeJpaRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 랭킹 경품입니다: " + id));
        entity.applyChanges(entity.getPrizeRank(), entity.getName(), entity.getBrand(), entity.getImageFileId(), true);
    }
}

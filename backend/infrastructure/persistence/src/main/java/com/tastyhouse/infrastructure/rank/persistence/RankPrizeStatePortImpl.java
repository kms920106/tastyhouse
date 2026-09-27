package com.tastyhouse.infrastructure.rank.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.rank.port.out.write.RankPrizeState;
import com.tastyhouse.application.rank.port.out.write.RankPrizeStatePort;

import static com.tastyhouse.infrastructure.rank.persistence.QRankPrizeJpaEntity.rankPrizeJpaEntity;

@Repository
public class RankPrizeStatePortImpl implements RankPrizeStatePort {
    private final JPAQueryFactory queryFactory;
    private final RankPrizeJpaRepository rankPrizeJpaRepository;

    public RankPrizeStatePortImpl(JPAQueryFactory queryFactory, RankPrizeJpaRepository rankPrizeJpaRepository) {
        this.queryFactory = queryFactory;
        this.rankPrizeJpaRepository = rankPrizeJpaRepository;
    }

    @Override
    public RankPrizeState save(RankPrizeState state) {
        if (state.id() == null) {
            RankPrizeJpaEntity saved = rankPrizeJpaRepository.save(RankPrizeMapper.toEntity(state));
            return RankPrizeMapper.toState(saved);
        }

        RankPrizeJpaEntity entity = rankPrizeJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 랭킹 경품입니다: " + state.id()));
        RankPrizeMapper.applyChanges(entity, state);
        return RankPrizeMapper.toState(entity);
    }

    @Override
    public Optional<RankPrizeState> findById(Long id) {
        RankPrizeJpaEntity entity = queryFactory
            .selectFrom(rankPrizeJpaEntity)
            .where(rankPrizeJpaEntity.id.eq(id), rankPrizeJpaEntity.deleted.isFalse())
            .fetchOne();
        return Optional.ofNullable(entity).map(RankPrizeMapper::toState);
    }

    @Override
    public void delete(Long id) {
        RankPrizeJpaEntity entity = rankPrizeJpaRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 랭킹 경품입니다: " + id));
        entity.applyChanges(entity.getPrizeRank(), entity.getName(), entity.getBrand(), entity.getImageFileId(), true);
    }
}

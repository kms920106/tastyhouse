package com.tastyhouse.infrastructure.rank.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.rank.port.out.write.RankPeriodState;
import com.tastyhouse.application.rank.port.out.write.RankPeriodStatePort;

import static com.tastyhouse.infrastructure.rank.persistence.QRankPeriodJpaEntity.rankPeriodJpaEntity;

@Repository
public class RankPeriodStatePortImpl implements RankPeriodStatePort {
    private final JPAQueryFactory queryFactory;
    private final RankPeriodJpaRepository rankPeriodJpaRepository;

    public RankPeriodStatePortImpl(JPAQueryFactory queryFactory, RankPeriodJpaRepository rankPeriodJpaRepository) {
        this.queryFactory = queryFactory;
        this.rankPeriodJpaRepository = rankPeriodJpaRepository;
    }

    @Override
    public RankPeriodState save(RankPeriodState state) {
        if (state.id() == null) {
            RankPeriodJpaEntity saved = rankPeriodJpaRepository.save(RankPeriodMapper.toEntity(state));
            return RankPeriodMapper.toState(saved);
        }

        RankPeriodJpaEntity entity = rankPeriodJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 랭킹 기간입니다: " + state.id()));
        RankPeriodMapper.applyChanges(entity, state);
        return RankPeriodMapper.toState(entity);
    }

    @Override
    public Optional<RankPeriodState> findById(Long id) {
        RankPeriodJpaEntity entity = queryFactory
            .selectFrom(rankPeriodJpaEntity)
            .where(rankPeriodJpaEntity.id.eq(id), rankPeriodJpaEntity.deleted.isFalse())
            .fetchOne();
        return Optional.ofNullable(entity).map(RankPeriodMapper::toState);
    }

    @Override
    public void delete(Long id) {
        RankPeriodJpaEntity entity = rankPeriodJpaRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 랭킹 기간입니다: " + id));
        entity.applyChanges(entity.getStartAt(), entity.getEndAt(), entity.isVisible(), true);
    }
}

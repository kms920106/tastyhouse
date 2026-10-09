package com.tastyhouse.infrastructure.jpa.rank.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.rank.model.RankPeriod;
import com.tastyhouse.domain.rank.vo.RankPeriodId;
import com.tastyhouse.application.rank.port.out.write.RankPeriodLoadPort;
import com.tastyhouse.application.rank.port.out.write.RankPeriodSavePort;

import static com.tastyhouse.infrastructure.jpa.rank.persistence.QRankPeriodJpaEntity.rankPeriodJpaEntity;

@Repository
class RankPeriodPersistenceAdapter implements RankPeriodLoadPort, RankPeriodSavePort {

    private final JPAQueryFactory queryFactory;
    private final RankPeriodJpaRepository rankPeriodJpaRepository;

    public RankPeriodPersistenceAdapter(JPAQueryFactory queryFactory, RankPeriodJpaRepository rankPeriodJpaRepository) {
        this.queryFactory = queryFactory;
        this.rankPeriodJpaRepository = rankPeriodJpaRepository;
    }

    @Override
    public RankPeriod save(RankPeriod rankPeriod) {
        if (rankPeriod.getId() == null) {
            RankPeriodJpaEntity saved = rankPeriodJpaRepository.save(RankPeriodMapper.toEntity(rankPeriod));
            return RankPeriodMapper.toDomain(saved);
        }

        RankPeriodJpaEntity entity = rankPeriodJpaRepository.findById(rankPeriod.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 랭킹 기간입니다: " + rankPeriod.getId()));
        RankPeriodMapper.applyChanges(entity, rankPeriod);
        return RankPeriodMapper.toDomain(entity);
    }

    @Override
    public Optional<RankPeriod> findActiveById(RankPeriodId id) {
        RankPeriodJpaEntity entity = queryFactory
            .selectFrom(rankPeriodJpaEntity)
            .where(rankPeriodJpaEntity.id.eq(id.value()), rankPeriodJpaEntity.deleted.isFalse())
            .fetchOne();
        return Optional.ofNullable(entity).map(RankPeriodMapper::toDomain);
    }

    @Override
    public void delete(RankPeriod rankPeriod) {
        Long id = rankPeriod.getId();
        RankPeriodJpaEntity entity = rankPeriodJpaRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 랭킹 기간입니다: " + id));
        entity.applyChanges(entity.getStartAt(), entity.getEndAt(), entity.isVisible(), true);
    }
}

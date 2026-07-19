package com.tastyhouse.infrastructure.rank.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.rank.domain.model.RankPeriod;
import com.tastyhouse.core.domain.rank.domain.repository.RankPeriodRepository;
import com.tastyhouse.core.domain.rank.domain.vo.RankPeriodId;
import com.tastyhouse.core.domain.rank.application.dto.result.RankPeriodResult;

import static com.tastyhouse.infrastructure.rank.persistence.QRankPeriodJpaEntity.rankPeriodJpaEntity;

@Repository
@RequiredArgsConstructor
public class RankPeriodRepositoryImpl implements RankPeriodRepository {

    private final JPAQueryFactory queryFactory;
    private final RankPeriodJpaRepository rankPeriodJpaRepository;

    @Override
    public RankPeriod save(RankPeriod rankPeriod) {
        if (rankPeriod.getId() == null) {
            RankPeriodJpaEntity saved = rankPeriodJpaRepository.save(RankPeriodMapper.toEntity(rankPeriod));
            return RankPeriodMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        RankPeriodJpaEntity entity = rankPeriodJpaRepository.findById(rankPeriod.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 랭킹 기간입니다: " + rankPeriod.getId()));
        RankPeriodMapper.applyChanges(entity, rankPeriod);
        return RankPeriodMapper.toDomain(entity);
    }

    @Override
    public Optional<RankPeriod> findById(RankPeriodId id) {
        RankPeriodJpaEntity entity = queryFactory
            .selectFrom(rankPeriodJpaEntity)
            .where(rankPeriodJpaEntity.id.eq(id.value()), rankPeriodJpaEntity.deleted.isFalse())
            .fetchOne();
        return Optional.ofNullable(entity).map(RankPeriodMapper::toDomain);
    }

    @Override
    public List<RankPeriodResult> findAllPeriods() {
        return queryFactory
            .select(Projections.constructor(RankPeriodResult.class,
                rankPeriodJpaEntity.id,
                rankPeriodJpaEntity.startAt,
                rankPeriodJpaEntity.endAt,
                rankPeriodJpaEntity.visible,
                rankPeriodJpaEntity.createdAt,
                rankPeriodJpaEntity.updatedAt
            ))
            .from(rankPeriodJpaEntity)
            .where(rankPeriodJpaEntity.deleted.isFalse())
            .orderBy(rankPeriodJpaEntity.startAt.desc())
            .fetch();
    }

    @Override
    public Optional<RankPeriodResult> findPeriodById(RankPeriodId id) {
        RankPeriodResult result = queryFactory
            .select(Projections.constructor(RankPeriodResult.class,
                rankPeriodJpaEntity.id,
                rankPeriodJpaEntity.startAt,
                rankPeriodJpaEntity.endAt,
                rankPeriodJpaEntity.visible,
                rankPeriodJpaEntity.createdAt,
                rankPeriodJpaEntity.updatedAt
            ))
            .from(rankPeriodJpaEntity)
            .where(rankPeriodJpaEntity.id.eq(id.value()), rankPeriodJpaEntity.deleted.isFalse())
            .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public void delete(RankPeriod rankPeriod) {
        // 하드 삭제 -> 소프트 삭제 전환: 필터 없는 순수 PK 조회(managed) 후 deleted 플래그만 갱신해 flush.
        RankPeriodJpaEntity entity = rankPeriodJpaRepository.findById(rankPeriod.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 랭킹 기간입니다: " + rankPeriod.getId()));
        entity.applyChanges(entity.getStartAt(), entity.getEndAt(), entity.isVisible(), true);
    }
}

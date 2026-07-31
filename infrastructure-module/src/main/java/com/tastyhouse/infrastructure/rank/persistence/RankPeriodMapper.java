package com.tastyhouse.infrastructure.rank.persistence;

import com.tastyhouse.domain.rank.domain.model.RankPeriod;

/**
 * 랭킹 기간 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class RankPeriodMapper {

    private RankPeriodMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static RankPeriod toDomain(RankPeriodJpaEntity entity) {
        return RankPeriod.reconstitute(
            entity.getId(),
            entity.getStartAt(),
            entity.getEndAt(),
            entity.isVisible(),
            entity.isDeleted(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static RankPeriodJpaEntity toEntity(RankPeriod domain) {
        return RankPeriodJpaEntity.create(
            domain.getStartAt(),
            domain.getEndAt(),
            domain.isVisible(),
            domain.isDeleted()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(RankPeriodJpaEntity entity, RankPeriod domain) {
        entity.applyChanges(
            domain.getStartAt(),
            domain.getEndAt(),
            domain.isVisible(),
            domain.isDeleted()
        );
    }
}

package com.tastyhouse.infrastructure.point.persistence;

import com.tastyhouse.domain.point.domain.model.PointHistory;

/**
 * 회원 포인트 변동 이력 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class PointHistoryMapper {

    private PointHistoryMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static PointHistory toDomain(PointHistoryJpaEntity entity) {
        return PointHistory.reconstitute(
            entity.getId(),
            entity.getMemberId(),
            entity.getPointType(),
            entity.getPointAmount(),
            entity.getReason(),
            entity.getCreatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태, insert 전용).
     */
    static PointHistoryJpaEntity toEntity(PointHistory domain) {
        return PointHistoryJpaEntity.create(
            domain.getMemberId(),
            domain.getPointType(),
            domain.getPointAmount(),
            domain.getReason()
        );
    }
}

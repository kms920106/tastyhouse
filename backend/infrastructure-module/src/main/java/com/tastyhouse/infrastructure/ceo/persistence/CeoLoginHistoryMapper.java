package com.tastyhouse.infrastructure.ceo.persistence;

import com.tastyhouse.domain.ceo.model.CeoLoginHistory;
import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 점주 로그인 이력 도메인 모델 ↔ JPA 엔티티 변환기. append-only 이력이라 update
 * 경로({@code applyChanges})가 없다.
 */
final class CeoLoginHistoryMapper {

    private CeoLoginHistoryMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static CeoLoginHistory toDomain(CeoLoginHistoryJpaEntity entity) {
        return CeoLoginHistory.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getCeoId(), CeoId::of),
            entity.getResult(),
            entity.getFailureReason(),
            entity.getIpAddress(),
            entity.getUserAgent(),
            entity.getCreatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static CeoLoginHistoryJpaEntity toEntity(CeoLoginHistory domain) {
        return CeoLoginHistoryJpaEntity.create(
            IdMapping.raw(domain.getCeoId(), CeoId::value),
            domain.getResult(),
            domain.getFailureReason(),
            domain.getIpAddress(),
            domain.getUserAgent()
        );
    }
}

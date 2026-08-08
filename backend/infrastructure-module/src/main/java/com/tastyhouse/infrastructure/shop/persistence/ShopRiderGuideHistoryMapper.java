package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopRiderGuideHistory;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 라이더 안내 이력 도메인 모델 ↔ JPA 엔티티 변환기. append-only 이력이라 update 경로는 없다.
 */
final class ShopRiderGuideHistoryMapper {

    private ShopRiderGuideHistoryMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static ShopRiderGuideHistory toDomain(ShopRiderGuideHistoryJpaEntity entity) {
        return ShopRiderGuideHistory.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getActorType(),
            entity.getActorId(),
            entity.getActionType(),
            entity.getPreviousVisitGuide(),
            entity.getNewVisitGuide(),
            entity.getReason(),
            entity.getCreatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ShopRiderGuideHistoryJpaEntity toEntity(ShopRiderGuideHistory domain) {
        return ShopRiderGuideHistoryJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getActorType(),
            domain.getActorId(),
            domain.getActionType(),
            domain.getPreviousVisitGuide(),
            domain.getNewVisitGuide(),
            domain.getReason()
        );
    }
}

package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopChangeHistory;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 가게 변경이력 도메인 모델 ↔ JPA 엔티티 변환기. append-only 이력이라 update 경로({@code applyChanges})가
 * 없다.
 */
final class ShopChangeHistoryMapper {

    private ShopChangeHistoryMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static ShopChangeHistory toDomain(ShopChangeHistoryJpaEntity entity) {
        return ShopChangeHistory.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getCategory(),
            entity.getChangeType(),
            entity.getActionType(),
            entity.getActorType(),
            entity.getActorId(),
            entity.getPreviousValue(),
            entity.getNewValue(),
            entity.getCreatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ShopChangeHistoryJpaEntity toEntity(ShopChangeHistory domain) {
        return ShopChangeHistoryJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getCategory(),
            domain.getChangeType(),
            domain.getActionType(),
            domain.getActorType(),
            domain.getActorId(),
            domain.getPreviousValue(),
            domain.getNewValue()
        );
    }
}

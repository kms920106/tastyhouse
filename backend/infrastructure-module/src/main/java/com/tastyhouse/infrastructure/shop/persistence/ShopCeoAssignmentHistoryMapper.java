package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.shop.model.ShopCeoAssignmentHistory;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 가게-점주 접근권한 이력 도메인 모델 ↔ JPA 엔티티 변환기. append-only 이력이라 update
 * 경로({@code applyChanges})가 없다.
 */
final class ShopCeoAssignmentHistoryMapper {

    private ShopCeoAssignmentHistoryMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static ShopCeoAssignmentHistory toDomain(ShopCeoAssignmentHistoryJpaEntity entity) {
        return ShopCeoAssignmentHistory.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            IdMapping.vo(entity.getCeoId(), CeoId::of),
            entity.getActionType(),
            entity.getActorAdminId(),
            entity.getCreatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ShopCeoAssignmentHistoryJpaEntity toEntity(ShopCeoAssignmentHistory domain) {
        return ShopCeoAssignmentHistoryJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            IdMapping.raw(domain.getCeoId(), CeoId::value),
            domain.getActionType(),
            domain.getActorAdminId()
        );
    }
}

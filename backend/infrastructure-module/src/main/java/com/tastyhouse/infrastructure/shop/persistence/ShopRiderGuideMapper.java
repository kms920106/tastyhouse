package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopRiderGuide;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 라이더 안내 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class ShopRiderGuideMapper {

    private ShopRiderGuideMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static ShopRiderGuide toDomain(ShopRiderGuideJpaEntity entity) {
        return ShopRiderGuide.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getVisitGuide(),
            entity.getPickupRoadAddress(),
            entity.getPickupLotAddress(),
            entity.getPickupDetailAddress(),
            entity.getPickupLatitude(),
            entity.getPickupLongitude(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ShopRiderGuideJpaEntity toEntity(ShopRiderGuide domain) {
        return ShopRiderGuideJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getVisitGuide(),
            domain.getPickupRoadAddress(),
            domain.getPickupLotAddress(),
            domain.getPickupDetailAddress(),
            domain.getPickupLatitude(),
            domain.getPickupLongitude()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(ShopRiderGuideJpaEntity entity, ShopRiderGuide domain) {
        entity.applyChanges(
            domain.getVisitGuide(),
            domain.getPickupRoadAddress(),
            domain.getPickupLotAddress(),
            domain.getPickupDetailAddress(),
            domain.getPickupLatitude(),
            domain.getPickupLongitude()
        );
    }
}

package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopSuspension;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 상점 영업 임시중지 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class ShopSuspensionMapper {

    private ShopSuspensionMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static ShopSuspension toDomain(ShopSuspensionJpaEntity entity) {
        return ShopSuspension.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getReason(),
            entity.getOrderMethod(),
            entity.getStartAt(),
            entity.getEndAt(),
            entity.getReleasedAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ShopSuspensionJpaEntity toEntity(ShopSuspension domain) {
        return ShopSuspensionJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getReason(),
            domain.getOrderMethod(),
            domain.getStartAt(),
            domain.getEndAt(),
            domain.getReleasedAt()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(ShopSuspensionJpaEntity entity, ShopSuspension domain) {
        entity.applyChanges(domain.getReleasedAt());
    }
}

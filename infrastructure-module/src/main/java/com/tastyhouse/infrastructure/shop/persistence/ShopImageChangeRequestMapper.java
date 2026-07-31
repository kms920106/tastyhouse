package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.domain.model.ShopImageChangeRequest;

/**
 * 가게 이미지 변경 승인요청 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class ShopImageChangeRequestMapper {

    private ShopImageChangeRequestMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static ShopImageChangeRequest toDomain(ShopImageChangeRequestJpaEntity entity) {
        return ShopImageChangeRequest.reconstitute(
            entity.getId(),
            entity.getShopId(),
            entity.getImageType(),
            entity.getImageFileId(),
            entity.getStatus(),
            entity.getRejectReason(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ShopImageChangeRequestJpaEntity toEntity(ShopImageChangeRequest domain) {
        return ShopImageChangeRequestJpaEntity.create(
            domain.getShopId(),
            domain.getImageType(),
            domain.getImageFileId(),
            domain.getStatus(),
            domain.getRejectReason()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(ShopImageChangeRequestJpaEntity entity, ShopImageChangeRequest domain) {
        entity.applyChanges(
            domain.getStatus(),
            domain.getRejectReason()
        );
    }
}

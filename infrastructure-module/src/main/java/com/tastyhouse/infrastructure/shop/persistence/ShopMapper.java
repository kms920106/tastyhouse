package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.core.domain.shop.domain.model.Shop;

/**
 * 상점 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class ShopMapper {

    private ShopMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static Shop toDomain(ShopJpaEntity entity) {
        return Shop.reconstitute(
            entity.getId(),
            entity.getCeoId(),
            entity.getStationId(),
            entity.getName(),
            entity.getLatitude(),
            entity.getLongitude(),
            entity.getRating(),
            entity.getRoadAddress(),
            entity.getLotAddress(),
            entity.getPhoneNumber(),
            entity.getThumbnailImageFileId(),
            entity.getTrademarkImageFileId(),
            entity.isPermanentlyClosed(),
            entity.isHidden(),
            entity.isClosedOnPublicHolidays(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ShopJpaEntity toEntity(Shop domain) {
        return ShopJpaEntity.create(
            domain.getCeoId(),
            domain.getStationId(),
            domain.getName(),
            domain.getLatitude(),
            domain.getLongitude(),
            domain.getRating(),
            domain.getRoadAddress(),
            domain.getLotAddress(),
            domain.getPhoneNumber(),
            domain.getThumbnailImageFileId(),
            domain.getTrademarkImageFileId(),
            domain.isPermanentlyClosed(),
            domain.isHidden(),
            domain.isClosedOnPublicHolidays()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(ShopJpaEntity entity, Shop domain) {
        entity.applyChanges(
            domain.getCeoId(),
            domain.getStationId(),
            domain.getName(),
            domain.getLatitude(),
            domain.getLongitude(),
            domain.getRating(),
            domain.getRoadAddress(),
            domain.getLotAddress(),
            domain.getPhoneNumber(),
            domain.getThumbnailImageFileId(),
            domain.getTrademarkImageFileId(),
            domain.isPermanentlyClosed(),
            domain.isHidden(),
            domain.isClosedOnPublicHolidays()
        );
    }
}

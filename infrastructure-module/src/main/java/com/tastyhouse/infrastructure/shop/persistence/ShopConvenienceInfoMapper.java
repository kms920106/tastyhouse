package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.domain.model.ShopConvenienceInfo;

/**
 * 가게 편의정보 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class ShopConvenienceInfoMapper {

    private ShopConvenienceInfoMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static ShopConvenienceInfo toDomain(ShopConvenienceInfoJpaEntity entity) {
        return ShopConvenienceInfo.reconstitute(
            entity.getId(),
            entity.getShopId(),
            entity.isParkingAvailable(),
            entity.isParkingPaid(),
            entity.isValetAvailable(),
            entity.isValetPaid(),
            entity.getDirectionsGuide(),
            entity.getDisplayLatitude(),
            entity.getDisplayLongitude(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ShopConvenienceInfoJpaEntity toEntity(ShopConvenienceInfo domain) {
        return ShopConvenienceInfoJpaEntity.create(
            domain.getShopId(),
            domain.isParkingAvailable(),
            domain.isParkingPaid(),
            domain.isValetAvailable(),
            domain.isValetPaid(),
            domain.getDirectionsGuide(),
            domain.getDisplayLatitude(),
            domain.getDisplayLongitude()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(ShopConvenienceInfoJpaEntity entity, ShopConvenienceInfo domain) {
        entity.applyChanges(
            domain.isParkingAvailable(),
            domain.isParkingPaid(),
            domain.isValetAvailable(),
            domain.isValetPaid(),
            domain.getDirectionsGuide(),
            domain.getDisplayLatitude(),
            domain.getDisplayLongitude()
        );
    }
}

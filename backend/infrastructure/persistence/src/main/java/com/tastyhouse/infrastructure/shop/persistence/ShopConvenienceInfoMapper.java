package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopConvenienceInfo;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ShopConvenienceInfoMapper {
    private ShopConvenienceInfoMapper() {
    }

    static ShopConvenienceInfo toDomain(ShopConvenienceInfoJpaEntity entity) {
        return ShopConvenienceInfo.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
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

    static ShopConvenienceInfoJpaEntity toEntity(ShopConvenienceInfo domain) {
        return ShopConvenienceInfoJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.isParkingAvailable(),
            domain.isParkingPaid(),
            domain.isValetAvailable(),
            domain.isValetPaid(),
            domain.getDirectionsGuide(),
            domain.getDisplayLatitude(),
            domain.getDisplayLongitude()
        );
    }

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

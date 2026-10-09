package com.tastyhouse.infrastructure.jpa.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopConvenienceInfo;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopConvenienceInfoMapper {

    private ShopConvenienceInfoMapper() {
    }

    static ShopConvenienceInfo toDomain(ShopConvenienceInfoJpaEntity entity) {
        return ShopConvenienceInfo.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
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

    static ShopConvenienceInfoJpaEntity toEntity(ShopConvenienceInfo shopConvenienceInfo) {
        return ShopConvenienceInfoJpaEntity.create(
            shopConvenienceInfo.getShopId() == null ? null : shopConvenienceInfo.getShopId().value(),
            shopConvenienceInfo.isParkingAvailable(),
            shopConvenienceInfo.isParkingPaid(),
            shopConvenienceInfo.isValetAvailable(),
            shopConvenienceInfo.isValetPaid(),
            shopConvenienceInfo.getDirectionsGuide(),
            shopConvenienceInfo.getDisplayLatitude(),
            shopConvenienceInfo.getDisplayLongitude()
        );
    }

    static void applyChanges(ShopConvenienceInfoJpaEntity entity, ShopConvenienceInfo shopConvenienceInfo) {
        entity.applyChanges(
            shopConvenienceInfo.isParkingAvailable(),
            shopConvenienceInfo.isParkingPaid(),
            shopConvenienceInfo.isValetAvailable(),
            shopConvenienceInfo.isValetPaid(),
            shopConvenienceInfo.getDirectionsGuide(),
            shopConvenienceInfo.getDisplayLatitude(),
            shopConvenienceInfo.getDisplayLongitude()
        );
    }
}

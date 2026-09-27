package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ShopConvenienceInfoState;

final class ShopConvenienceInfoMapper {
    private ShopConvenienceInfoMapper() {
    }

    static ShopConvenienceInfoState toState(ShopConvenienceInfoJpaEntity entity) {
        return new ShopConvenienceInfoState(
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

    static ShopConvenienceInfoJpaEntity toEntity(ShopConvenienceInfoState state) {
        return ShopConvenienceInfoJpaEntity.create(
            state.shopId(),
            state.parkingAvailable(),
            state.parkingPaid(),
            state.valetAvailable(),
            state.valetPaid(),
            state.directionsGuide(),
            state.displayLatitude(),
            state.displayLongitude()
        );
    }

    static void applyChanges(ShopConvenienceInfoJpaEntity entity, ShopConvenienceInfoState state) {
        entity.applyChanges(
            state.parkingAvailable(),
            state.parkingPaid(),
            state.valetAvailable(),
            state.valetPaid(),
            state.directionsGuide(),
            state.displayLatitude(),
            state.displayLongitude()
        );
    }
}

package com.tastyhouse.application.shop.store;

import com.tastyhouse.domain.shop.model.ShopConvenienceInfo;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopConvenienceInfoState;

final class ShopConvenienceInfoStateMapper {
    private ShopConvenienceInfoStateMapper() {
    }

    static ShopConvenienceInfo toDomain(ShopConvenienceInfoState state) {
        return ShopConvenienceInfo.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.parkingAvailable(),
            state.parkingPaid(),
            state.valetAvailable(),
            state.valetPaid(),
            state.directionsGuide(),
            state.displayLatitude(),
            state.displayLongitude(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static ShopConvenienceInfoState toState(ShopConvenienceInfo shopConvenienceInfo) {
        return new ShopConvenienceInfoState(
            shopConvenienceInfo.getId(),
            shopConvenienceInfo.getShopId() == null ? null : shopConvenienceInfo.getShopId().value(),
            shopConvenienceInfo.isParkingAvailable(),
            shopConvenienceInfo.isParkingPaid(),
            shopConvenienceInfo.isValetAvailable(),
            shopConvenienceInfo.isValetPaid(),
            shopConvenienceInfo.getDirectionsGuide(),
            shopConvenienceInfo.getDisplayLatitude(),
            shopConvenienceInfo.getDisplayLongitude(),
            shopConvenienceInfo.getCreatedAt(),
            shopConvenienceInfo.getUpdatedAt()
        );
    }
}

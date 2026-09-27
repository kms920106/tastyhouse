package com.tastyhouse.application.shop.store;

import com.tastyhouse.application.shop.port.out.write.ShopRiderGuideState;
import com.tastyhouse.domain.shop.model.ShopRiderGuide;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopRiderGuideStateMapper {
    private ShopRiderGuideStateMapper() {
    }

    static ShopRiderGuide toDomain(ShopRiderGuideState state) {
        return ShopRiderGuide.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.visitGuide(),
            state.pickupRoadAddress(),
            state.pickupLotAddress(),
            state.pickupDetailAddress(),
            state.pickupLatitude(),
            state.pickupLongitude(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static ShopRiderGuideState toState(ShopRiderGuide shopRiderGuide) {
        return new ShopRiderGuideState(
            shopRiderGuide.getId(),
            shopRiderGuide.getShopId() == null ? null : shopRiderGuide.getShopId().value(),
            shopRiderGuide.getVisitGuide(),
            shopRiderGuide.getPickupRoadAddress(),
            shopRiderGuide.getPickupLotAddress(),
            shopRiderGuide.getPickupDetailAddress(),
            shopRiderGuide.getPickupLatitude(),
            shopRiderGuide.getPickupLongitude(),
            shopRiderGuide.getCreatedAt(),
            shopRiderGuide.getUpdatedAt()
        );
    }
}

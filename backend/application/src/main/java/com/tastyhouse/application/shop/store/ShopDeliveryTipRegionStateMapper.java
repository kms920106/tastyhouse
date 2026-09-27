package com.tastyhouse.application.shop.store;

import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipRegion;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryTipRegionState;

final class ShopDeliveryTipRegionStateMapper {
    private ShopDeliveryTipRegionStateMapper() {
    }

    static ShopDeliveryTipRegion toDomain(ShopDeliveryTipRegionState state) {
        return ShopDeliveryTipRegion.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.adminDongId() == null ? null : AdminDongId.of(state.adminDongId()),
            state.tipAmount()
        );
    }

    static ShopDeliveryTipRegionState toState(ShopDeliveryTipRegion shopDeliveryTipRegion) {
        return new ShopDeliveryTipRegionState(
            shopDeliveryTipRegion.getId(),
            shopDeliveryTipRegion.getShopId() == null ? null : shopDeliveryTipRegion.getShopId().value(),
            shopDeliveryTipRegion.getAdminDongId() == null ? null : shopDeliveryTipRegion.getAdminDongId().value(),
            shopDeliveryTipRegion.getTipAmount()
        );
    }
}

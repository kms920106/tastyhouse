package com.tastyhouse.application.shop.store;

import com.tastyhouse.application.shop.port.out.write.ShopDeliveryTipTierState;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipTier;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopDeliveryTipTierStateMapper {
    private ShopDeliveryTipTierStateMapper() {
    }

    static ShopDeliveryTipTier toDomain(ShopDeliveryTipTierState state) {
        return ShopDeliveryTipTier.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.tierOrder(),
            state.minOrderAmount(),
            state.tipAmount()
        );
    }

    static ShopDeliveryTipTierState toState(ShopDeliveryTipTier shopDeliveryTipTier) {
        return new ShopDeliveryTipTierState(
            shopDeliveryTipTier.getId(),
            shopDeliveryTipTier.getShopId() == null ? null : shopDeliveryTipTier.getShopId().value(),
            shopDeliveryTipTier.getTierOrder(),
            shopDeliveryTipTier.getMinOrderAmount(),
            shopDeliveryTipTier.getTipAmount()
        );
    }
}

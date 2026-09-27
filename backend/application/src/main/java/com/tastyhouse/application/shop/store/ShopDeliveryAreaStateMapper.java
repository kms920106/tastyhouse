package com.tastyhouse.application.shop.store;

import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaState;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.model.DeliveryAreaSource;
import com.tastyhouse.domain.shop.model.ShopDeliveryArea;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopDeliveryAreaStateMapper {
    private ShopDeliveryAreaStateMapper() {
    }

    static ShopDeliveryArea toDomain(ShopDeliveryAreaState state) {
        return ShopDeliveryArea.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.adminDongId() == null ? null : AdminDongId.of(state.adminDongId()),
            state.source() == null ? DeliveryAreaSource.MANUAL : DeliveryAreaSource.valueOf(state.source())
        );
    }

    static ShopDeliveryAreaState toState(ShopDeliveryArea shopDeliveryArea) {
        return new ShopDeliveryAreaState(
            shopDeliveryArea.getId(),
            shopDeliveryArea.getShopId() == null ? null : shopDeliveryArea.getShopId().value(),
            shopDeliveryArea.getAdminDongId() == null ? null : shopDeliveryArea.getAdminDongId().value(),
            shopDeliveryArea.getSource() == null ? null : shopDeliveryArea.getSource().name()
        );
    }
}

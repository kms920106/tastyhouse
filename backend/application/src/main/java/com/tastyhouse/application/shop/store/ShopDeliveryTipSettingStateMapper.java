package com.tastyhouse.application.shop.store;

import com.tastyhouse.domain.shop.model.DeliveryTipDistanceUnit;
import com.tastyhouse.domain.shop.model.DeliveryTipExtraType;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSetting;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryTipSettingState;

final class ShopDeliveryTipSettingStateMapper {
    private ShopDeliveryTipSettingStateMapper() {
    }

    static ShopDeliveryTipSetting toDomain(ShopDeliveryTipSettingState state) {
        return ShopDeliveryTipSetting.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.extraTipType() == null ? null : DeliveryTipExtraType.valueOf(state.extraTipType()),
            state.baseDistanceMeters(),
            state.surchargeUnit() == null ? null : DeliveryTipDistanceUnit.valueOf(state.surchargeUnit()),
            state.surchargeAmount(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static ShopDeliveryTipSettingState toState(ShopDeliveryTipSetting shopDeliveryTipSetting) {
        return new ShopDeliveryTipSettingState(
            shopDeliveryTipSetting.getId(),
            shopDeliveryTipSetting.getShopId() == null ? null : shopDeliveryTipSetting.getShopId().value(),
            shopDeliveryTipSetting.getExtraTipType() == null ? null : shopDeliveryTipSetting.getExtraTipType().name(),
            shopDeliveryTipSetting.getBaseDistanceMeters(),
            shopDeliveryTipSetting.getSurchargeUnit() == null ? null : shopDeliveryTipSetting.getSurchargeUnit().name(),
            shopDeliveryTipSetting.getSurchargeAmount(),
            shopDeliveryTipSetting.getCreatedAt(),
            shopDeliveryTipSetting.getUpdatedAt()
        );
    }
}

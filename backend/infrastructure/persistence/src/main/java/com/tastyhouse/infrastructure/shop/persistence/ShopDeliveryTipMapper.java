package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ShopDeliveryTipHolidayState;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryTipRegionState;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryTipScheduleState;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryTipSettingState;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryTipTierState;

final class ShopDeliveryTipMapper {
    private ShopDeliveryTipMapper() {
    }

    static ShopDeliveryTipSettingState toState(ShopDeliveryTipSettingJpaEntity entity) {
        return new ShopDeliveryTipSettingState(
            entity.getId(),
            entity.getShopId(),
            entity.getExtraTipType(),
            entity.getBaseDistanceMeters(),
            entity.getSurchargeUnit(),
            entity.getSurchargeAmount(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ShopDeliveryTipSettingJpaEntity toEntity(ShopDeliveryTipSettingState state) {
        return ShopDeliveryTipSettingJpaEntity.create(
            state.shopId(),
            state.extraTipType(),
            state.baseDistanceMeters(),
            state.surchargeUnit(),
            state.surchargeAmount()
        );
    }

    static void applyChanges(ShopDeliveryTipSettingJpaEntity entity, ShopDeliveryTipSettingState state) {
        entity.applyChanges(
            state.extraTipType(),
            state.baseDistanceMeters(),
            state.surchargeUnit(),
            state.surchargeAmount()
        );
    }

    static ShopDeliveryTipTierState toState(ShopDeliveryTipTierJpaEntity entity) {
        return new ShopDeliveryTipTierState(
            entity.getId(),
            entity.getShopId(),
            entity.getTierOrder(),
            entity.getMinOrderAmount(),
            entity.getTipAmount()
        );
    }

    static ShopDeliveryTipTierJpaEntity toEntity(ShopDeliveryTipTierState state) {
        return ShopDeliveryTipTierJpaEntity.create(
            state.shopId(),
            state.tierOrder(),
            state.minOrderAmount(),
            state.tipAmount()
        );
    }

    static ShopDeliveryTipRegionState toState(ShopDeliveryTipRegionJpaEntity entity) {
        return new ShopDeliveryTipRegionState(
            entity.getId(),
            entity.getShopId(),
            entity.getAdminDongId(),
            entity.getTipAmount()
        );
    }

    static ShopDeliveryTipRegionJpaEntity toEntity(ShopDeliveryTipRegionState state) {
        return ShopDeliveryTipRegionJpaEntity.create(
            state.shopId(),
            state.adminDongId(),
            state.tipAmount()
        );
    }

    static ShopDeliveryTipScheduleState toState(ShopDeliveryTipScheduleJpaEntity entity) {
        return new ShopDeliveryTipScheduleState(
            entity.getId(),
            entity.getShopId(),
            entity.getDayType(),
            entity.getStartTime(),
            entity.getEndTime(),
            entity.getTipAmount()
        );
    }

    static ShopDeliveryTipScheduleJpaEntity toEntity(ShopDeliveryTipScheduleState state) {
        return ShopDeliveryTipScheduleJpaEntity.create(
            state.shopId(),
            state.dayType(),
            state.startTime(),
            state.endTime(),
            state.tipAmount()
        );
    }

    static ShopDeliveryTipHolidayState toState(ShopDeliveryTipHolidayJpaEntity entity) {
        return new ShopDeliveryTipHolidayState(
            entity.getId(),
            entity.getShopId(),
            entity.getTipAmount()
        );
    }

    static ShopDeliveryTipHolidayJpaEntity toEntity(ShopDeliveryTipHolidayState state) {
        return ShopDeliveryTipHolidayJpaEntity.create(
            state.shopId(),
            state.tipAmount()
        );
    }

    static void applyChanges(ShopDeliveryTipHolidayJpaEntity entity, ShopDeliveryTipHolidayState state) {
        entity.applyChanges(state.tipAmount());
    }
}

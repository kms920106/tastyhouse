package com.tastyhouse.infrastructure.jpa.shop.persistence;

import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.domain.shop.model.DeliveryTipDistanceUnit;
import com.tastyhouse.domain.shop.model.DeliveryTipExtraType;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipHoliday;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipRegion;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSchedule;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSetting;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipTier;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopDeliveryTipMapper {

    private ShopDeliveryTipMapper() {
    }

    static ShopDeliveryTipSetting toDomain(ShopDeliveryTipSettingJpaEntity entity) {
        return ShopDeliveryTipSetting.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getExtraTipType() == null ? null : DeliveryTipExtraType.valueOf(entity.getExtraTipType()),
            entity.getBaseDistanceMeters(),
            entity.getSurchargeUnit() == null ? null : DeliveryTipDistanceUnit.valueOf(entity.getSurchargeUnit()),
            entity.getSurchargeAmount(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ShopDeliveryTipSettingJpaEntity toEntity(ShopDeliveryTipSetting shopDeliveryTipSetting) {
        return ShopDeliveryTipSettingJpaEntity.create(
            shopDeliveryTipSetting.getShopId() == null ? null : shopDeliveryTipSetting.getShopId().value(),
            shopDeliveryTipSetting.getExtraTipType() == null ? null : shopDeliveryTipSetting.getExtraTipType().name(),
            shopDeliveryTipSetting.getBaseDistanceMeters(),
            shopDeliveryTipSetting.getSurchargeUnit() == null ? null : shopDeliveryTipSetting.getSurchargeUnit().name(),
            shopDeliveryTipSetting.getSurchargeAmount()
        );
    }

    static void applyChanges(ShopDeliveryTipSettingJpaEntity entity, ShopDeliveryTipSetting shopDeliveryTipSetting) {
        entity.applyChanges(
            shopDeliveryTipSetting.getExtraTipType() == null ? null : shopDeliveryTipSetting.getExtraTipType().name(),
            shopDeliveryTipSetting.getBaseDistanceMeters(),
            shopDeliveryTipSetting.getSurchargeUnit() == null ? null : shopDeliveryTipSetting.getSurchargeUnit().name(),
            shopDeliveryTipSetting.getSurchargeAmount()
        );
    }

    static ShopDeliveryTipTier toDomain(ShopDeliveryTipTierJpaEntity entity) {
        return ShopDeliveryTipTier.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getTierOrder(),
            entity.getMinOrderAmount(),
            entity.getTipAmount()
        );
    }

    static ShopDeliveryTipTierJpaEntity toEntity(ShopDeliveryTipTier shopDeliveryTipTier) {
        return ShopDeliveryTipTierJpaEntity.create(
            shopDeliveryTipTier.getShopId() == null ? null : shopDeliveryTipTier.getShopId().value(),
            shopDeliveryTipTier.getTierOrder(),
            shopDeliveryTipTier.getMinOrderAmount(),
            shopDeliveryTipTier.getTipAmount()
        );
    }

    static ShopDeliveryTipRegion toDomain(ShopDeliveryTipRegionJpaEntity entity) {
        return ShopDeliveryTipRegion.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getAdminDongId() == null ? null : AdminDongId.of(entity.getAdminDongId()),
            entity.getTipAmount()
        );
    }

    static ShopDeliveryTipRegionJpaEntity toEntity(ShopDeliveryTipRegion shopDeliveryTipRegion) {
        return ShopDeliveryTipRegionJpaEntity.create(
            shopDeliveryTipRegion.getShopId() == null ? null : shopDeliveryTipRegion.getShopId().value(),
            shopDeliveryTipRegion.getAdminDongId() == null ? null : shopDeliveryTipRegion.getAdminDongId().value(),
            shopDeliveryTipRegion.getTipAmount()
        );
    }

    static ShopDeliveryTipSchedule toDomain(ShopDeliveryTipScheduleJpaEntity entity) {
        return ShopDeliveryTipSchedule.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getDayType() == null ? null : DayType.valueOf(entity.getDayType()),
            entity.getStartTime(),
            entity.getEndTime(),
            entity.getTipAmount()
        );
    }

    static ShopDeliveryTipScheduleJpaEntity toEntity(ShopDeliveryTipSchedule shopDeliveryTipSchedule) {
        return ShopDeliveryTipScheduleJpaEntity.create(
            shopDeliveryTipSchedule.getShopId() == null ? null : shopDeliveryTipSchedule.getShopId().value(),
            shopDeliveryTipSchedule.getDayType() == null ? null : shopDeliveryTipSchedule.getDayType().name(),
            shopDeliveryTipSchedule.getStartTime(),
            shopDeliveryTipSchedule.getEndTime(),
            shopDeliveryTipSchedule.getTipAmount()
        );
    }

    static ShopDeliveryTipHoliday toDomain(ShopDeliveryTipHolidayJpaEntity entity) {
        return ShopDeliveryTipHoliday.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getTipAmount()
        );
    }

    static ShopDeliveryTipHolidayJpaEntity toEntity(ShopDeliveryTipHoliday shopDeliveryTipHoliday) {
        return ShopDeliveryTipHolidayJpaEntity.create(
            shopDeliveryTipHoliday.getShopId() == null ? null : shopDeliveryTipHoliday.getShopId().value(),
            shopDeliveryTipHoliday.getTipAmount()
        );
    }

    static void applyChanges(ShopDeliveryTipHolidayJpaEntity entity, ShopDeliveryTipHoliday shopDeliveryTipHoliday) {
        entity.applyChanges(shopDeliveryTipHoliday.getTipAmount());
    }
}

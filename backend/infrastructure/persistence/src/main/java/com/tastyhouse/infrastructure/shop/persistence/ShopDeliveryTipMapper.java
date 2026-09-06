package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipHoliday;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipRegion;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSchedule;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSetting;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipTier;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ShopDeliveryTipMapper {
    private ShopDeliveryTipMapper() {
    }

    static ShopDeliveryTipSetting toDomain(ShopDeliveryTipSettingJpaEntity entity) {
        return ShopDeliveryTipSetting.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getExtraTipType(),
            entity.getBaseDistanceMeters(),
            entity.getSurchargeUnit(),
            entity.getSurchargeAmount(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ShopDeliveryTipSettingJpaEntity toEntity(ShopDeliveryTipSetting domain) {
        return ShopDeliveryTipSettingJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getExtraTipType(),
            domain.getBaseDistanceMeters(),
            domain.getSurchargeUnit(),
            domain.getSurchargeAmount()
        );
    }

    static void applyChanges(ShopDeliveryTipSettingJpaEntity entity, ShopDeliveryTipSetting domain) {
        entity.applyChanges(
            domain.getExtraTipType(),
            domain.getBaseDistanceMeters(),
            domain.getSurchargeUnit(),
            domain.getSurchargeAmount()
        );
    }

    static ShopDeliveryTipTier toDomain(ShopDeliveryTipTierJpaEntity entity) {
        return ShopDeliveryTipTier.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getTierOrder(),
            entity.getMinOrderAmount(),
            entity.getTipAmount()
        );
    }

    static ShopDeliveryTipTierJpaEntity toEntity(ShopDeliveryTipTier domain) {
        return ShopDeliveryTipTierJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getTierOrder(),
            domain.getMinOrderAmount(),
            domain.getTipAmount()
        );
    }

    static ShopDeliveryTipRegion toDomain(ShopDeliveryTipRegionJpaEntity entity) {
        return ShopDeliveryTipRegion.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            IdMapping.vo(entity.getAdminDongId(), AdminDongId::of),
            entity.getTipAmount()
        );
    }

    static ShopDeliveryTipRegionJpaEntity toEntity(ShopDeliveryTipRegion domain) {
        return ShopDeliveryTipRegionJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            IdMapping.raw(domain.getAdminDongId(), AdminDongId::value),
            domain.getTipAmount()
        );
    }

    static ShopDeliveryTipSchedule toDomain(ShopDeliveryTipScheduleJpaEntity entity) {
        return ShopDeliveryTipSchedule.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getDayType(),
            entity.getStartTime(),
            entity.getEndTime(),
            entity.getTipAmount()
        );
    }

    static ShopDeliveryTipScheduleJpaEntity toEntity(ShopDeliveryTipSchedule domain) {
        return ShopDeliveryTipScheduleJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getDayType(),
            domain.getStartTime(),
            domain.getEndTime(),
            domain.getTipAmount()
        );
    }

    static ShopDeliveryTipHoliday toDomain(ShopDeliveryTipHolidayJpaEntity entity) {
        return ShopDeliveryTipHoliday.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getTipAmount()
        );
    }

    static ShopDeliveryTipHolidayJpaEntity toEntity(ShopDeliveryTipHoliday domain) {
        return ShopDeliveryTipHolidayJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getTipAmount()
        );
    }

    static void applyChanges(ShopDeliveryTipHolidayJpaEntity entity, ShopDeliveryTipHoliday domain) {
        entity.applyChanges(domain.getTipAmount());
    }
}

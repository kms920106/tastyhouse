package com.tastyhouse.domain.shop.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopDeliveryTipHoliday;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipRegion;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSchedule;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSetting;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipTier;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ShopDeliveryTipRepository {
    Optional<ShopDeliveryTipSetting> findSettingByShopId(ShopId shopId);

    ShopDeliveryTipSetting saveSetting(ShopDeliveryTipSetting setting);

    List<ShopDeliveryTipTier> findTiersByShopId(ShopId shopId);

    List<ShopDeliveryTipTier> saveTiers(List<ShopDeliveryTipTier> tiers);

    void deleteTiersByShopId(ShopId shopId);

    List<ShopDeliveryTipRegion> findRegionTipsByShopId(ShopId shopId);

    long countRegionTipsByShopId(ShopId shopId);

    List<ShopDeliveryTipRegion> saveRegionTips(List<ShopDeliveryTipRegion> regionTips);

    void deleteRegionTipsByShopId(ShopId shopId);

    List<ShopDeliveryTipSchedule> findScheduleTipsByShopId(ShopId shopId);

    List<ShopDeliveryTipSchedule> saveScheduleTips(List<ShopDeliveryTipSchedule> scheduleTips);

    void deleteScheduleTipsByShopId(ShopId shopId);

    Optional<ShopDeliveryTipHoliday> findHolidayTipByShopId(ShopId shopId);

    ShopDeliveryTipHoliday saveHolidayTip(ShopDeliveryTipHoliday holidayTip);

    void deleteHolidayTipByShopId(ShopId shopId);
}

package com.tastyhouse.application.shop.port.out.write;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ShopDeliveryTipStatePort {
    Optional<ShopDeliveryTipSettingState> findSettingByShopId(Long shopId);

    ShopDeliveryTipSettingState saveSetting(ShopDeliveryTipSettingState setting);

    List<ShopDeliveryTipTierState> findTiersByShopId(Long shopId);

    List<ShopDeliveryTipTierState> saveTiers(List<ShopDeliveryTipTierState> tiers);

    void deleteTiersByShopId(Long shopId);

    List<ShopDeliveryTipRegionState> findRegionTipsByShopId(Long shopId);

    long countRegionTipsByShopId(Long shopId);

    List<ShopDeliveryTipRegionState> saveRegionTips(List<ShopDeliveryTipRegionState> regionTips);

    void deleteRegionTipsByShopId(Long shopId);

    List<ShopDeliveryTipScheduleState> findScheduleTipsByShopId(Long shopId);

    List<ShopDeliveryTipScheduleState> saveScheduleTips(List<ShopDeliveryTipScheduleState> scheduleTips);

    void deleteScheduleTipsByShopId(Long shopId);

    Optional<ShopDeliveryTipHolidayState> findHolidayTipByShopId(Long shopId);

    ShopDeliveryTipHolidayState saveHolidayTip(ShopDeliveryTipHolidayState holidayTip);

    void deleteHolidayTipByShopId(Long shopId);

    boolean existsRegionTipByShopIdAndAdminDongId(Long shopId, Long adminDongId);

    Set<Long> findRegionTipAdminDongIds(Long shopId);
}

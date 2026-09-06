package com.tastyhouse.application.shop.port.out;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ShopDeliveryTipQueryPort {

    Optional<ShopDeliveryTipSettingResult> findSetting(Long shopId);

    List<ShopDeliveryTipTierResult> findTiers(Long shopId);

    List<ShopDeliveryTipRegionResult> findRegionTips(Long shopId);

    List<ShopDeliveryTipScheduleResult> findScheduleTips(Long shopId);

    int findHolidayTipAmount(Long shopId);

    Map<Long, ShopDeliveryTipRangeResult> findTipRanges(List<Long> shopIds);

    ShopDeliveryTipRangeResult findTipRange(Long shopId);
}

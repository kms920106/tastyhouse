package com.tastyhouse.application.shop.port.out;

import java.util.List;

public record ShopDeliveryTipOwnerViewResult(
    ShopDeliveryTipSettingResult setting,
    List<ShopDeliveryTipTierResult> tiers,
    List<ShopDeliveryTipRegionResult> regions,
    List<ShopDeliveryTipScheduleResult> schedules,
    Integer holidayTipAmount
) {
}

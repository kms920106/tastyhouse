package com.tastyhouse.application.shop.port.out;

import java.util.List;

public record ShopDeliveryTipViewResult(
    Integer deliveryTip,
    int minDeliveryTip,
    int maxDeliveryTip,
    List<ShopDeliveryTipBreakdownItemResult> breakdown,
    List<ShopDeliveryTipTierResult> tiers,
    String extraTipType,
    ShopDeliveryTipSettingResult distance,
    List<ShopDeliveryTipRegionResult> regions,
    List<ShopDeliveryTipScheduleItemResult> schedules,
    int holidayTipAmount
) {
}

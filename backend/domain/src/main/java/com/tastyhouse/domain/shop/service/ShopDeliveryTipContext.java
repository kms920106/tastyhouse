package com.tastyhouse.domain.shop.service;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipHoliday;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipRegion;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSchedule;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSetting;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipTier;

public record ShopDeliveryTipContext(
    OrderMethod orderMethod,
    int orderAmountAfterProductDiscount,
    Double deliveryDistanceMeters,
    AdminDongId deliveryAdminDongId,
    LocalDateTime orderedAt,
    boolean publicHoliday,
    ShopDeliveryTipSetting setting,
    List<ShopDeliveryTipTier> tiers,
    List<ShopDeliveryTipRegion> regionTips,
    List<ShopDeliveryTipSchedule> scheduleTips,
    ShopDeliveryTipHoliday holidayTip
) {
    public ShopDeliveryTipContext {
        tiers = tiers == null ? List.of() : List.copyOf(tiers);
        regionTips = regionTips == null ? List.of() : List.copyOf(regionTips);
        scheduleTips = scheduleTips == null ? List.of() : List.copyOf(scheduleTips);
    }

    public static ShopDeliveryTipContext of(
        OrderMethod orderMethod,
        int orderAmountAfterProductDiscount,
        Double deliveryDistanceMeters,
        AdminDongId deliveryAdminDongId,
        LocalDateTime orderedAt,
        boolean publicHoliday,
        ShopDeliveryTipSetting setting,
        List<ShopDeliveryTipTier> tiers,
        List<ShopDeliveryTipRegion> regionTips,
        List<ShopDeliveryTipSchedule> scheduleTips,
        ShopDeliveryTipHoliday holidayTip
    ) {
        return new ShopDeliveryTipContext(
            orderMethod,
            orderAmountAfterProductDiscount,
            deliveryDistanceMeters,
            deliveryAdminDongId,
            orderedAt,
            publicHoliday,
            setting,
            tiers,
            regionTips,
            scheduleTips,
            holidayTip
        );
    }
}

package com.tastyhouse.domain.shop.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipHoliday;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipRegion;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSchedule;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSetting;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipTier;

public class ShopDeliveryTipCalculator {
    public ShopDeliveryTipBreakdown calculate(ShopDeliveryTipContext context) {
        if (context == null || context.orderMethod() != OrderMethod.DELIVERY) {
            return ShopDeliveryTipBreakdown.none();
        }

        int baseTipAmount = calculateBaseTip(context.tiers(), context.orderAmountAfterProductDiscount());
        int distanceTipAmount = calculateDistanceTip(context.setting(), context.deliveryDistanceMeters());
        int regionTipAmount = calculateRegionTip(
            context.setting(), context.regionTips(), context.deliveryAdminDongId()
        );
        int holidayTipAmount = calculateHolidayTip(context.publicHoliday(), context.holidayTip());

        int scheduleTipAmount = holidayTipAmount > 0
            ? 0
            : calculateScheduleTip(context.scheduleTips(), context.orderedAt(), context.publicHoliday());

        return ShopDeliveryTipBreakdown.of(
            baseTipAmount,
            distanceTipAmount,
            regionTipAmount,
            scheduleTipAmount,
            holidayTipAmount
        );
    }

    private int calculateBaseTip(List<ShopDeliveryTipTier> tiers, int orderAmount) {
        if (tiers.isEmpty()) {
            return 0;
        }

        return tiers.stream()
            .filter(tier -> tier.covers(orderAmount))
            .max(Comparator.comparingInt(ShopDeliveryTipTier::getMinOrderAmount))
            .or(() -> tiers.stream().min(Comparator.comparingInt(ShopDeliveryTipTier::getMinOrderAmount)))
            .map(ShopDeliveryTipTier::getTipAmount)
            .orElse(0);
    }

    private int calculateDistanceTip(ShopDeliveryTipSetting setting, Double distanceMeters) {
        if (setting == null || !setting.usesDistance() || distanceMeters == null) {
            return 0;
        }
        return setting.calculateDistanceSurcharge(distanceMeters);
    }

    private int calculateRegionTip(
        ShopDeliveryTipSetting setting,
        List<ShopDeliveryTipRegion> regionTips,
        AdminDongId deliveryAdminDongId
    ) {
        if (setting == null || !setting.usesRegion() || deliveryAdminDongId == null) {
            return 0;
        }

        return regionTips.stream()
            .filter(regionTip -> regionTip.matches(deliveryAdminDongId))
            .findFirst()
            .map(ShopDeliveryTipRegion::getTipAmount)
            .orElse(0);
    }

    private int calculateHolidayTip(boolean publicHoliday, ShopDeliveryTipHoliday holidayTip) {
        if (!publicHoliday || holidayTip == null) {
            return 0;
        }
        return holidayTip.getTipAmount();
    }

    private int calculateScheduleTip(
        List<ShopDeliveryTipSchedule> scheduleTips,
        LocalDateTime orderedAt,
        boolean publicHoliday
    ) {
        if (scheduleTips.isEmpty() || orderedAt == null) {
            return 0;
        }

        return selectApplicableSchedule(scheduleTips, orderedAt, publicHoliday)
            .map(ShopDeliveryTipSchedule::getTipAmount)
            .orElse(0);
    }

    private Optional<ShopDeliveryTipSchedule> selectApplicableSchedule(
        List<ShopDeliveryTipSchedule> scheduleTips,
        LocalDateTime orderedAt,
        boolean publicHoliday
    ) {
        LocalTime time = orderedAt.toLocalTime();
        DayOfWeek dayOfWeek = orderedAt.getDayOfWeek();

        ShopDeliveryTipSchedule daily = null;
        ShopDeliveryTipSchedule weekGroup = null;

        for (ShopDeliveryTipSchedule scheduleTip : scheduleTips) {
            if (!scheduleTip.covers(time, dayOfWeek, publicHoliday)) {
                continue;
            }

            DayType dayType = scheduleTip.getDayType();
            if (dayType.isSpecificDay(dayOfWeek)) {
                return Optional.of(scheduleTip);
            }
            if (dayType == DayType.WEEKEND || dayType == DayType.WEEKDAY) {
                weekGroup = scheduleTip;
            } else if (dayType == DayType.DAILY) {
                daily = scheduleTip;
            }
        }

        if (weekGroup != null) {
            return Optional.of(weekGroup);
        }
        return Optional.ofNullable(daily);
    }
}

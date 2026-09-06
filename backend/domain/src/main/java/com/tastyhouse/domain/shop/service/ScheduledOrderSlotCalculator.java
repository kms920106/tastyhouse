package com.tastyhouse.domain.shop.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.ScheduledOrderPolicy;
import com.tastyhouse.domain.shop.model.ScheduledOrderSlot;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;

public class ScheduledOrderSlotCalculator {
    private static final boolean PUBLIC_HOLIDAY = false;

    private static final int MAX_SLOT_CANDIDATES = 200;

    private final ShopOperatingStatusCalculator shopOperatingStatusCalculator;

    public ScheduledOrderSlotCalculator(ShopOperatingStatusCalculator shopOperatingStatusCalculator) {
        this.shopOperatingStatusCalculator = shopOperatingStatusCalculator;
    }

    public List<ScheduledOrderSlot> calculate(ScheduledOrderSlotContext context) {
        Shop shop = context.shop();
        OrderMethod orderMethod = context.orderMethod();

        if (!shop.isScheduledOrderEnabled() || !ScheduledOrderPolicy.supports(orderMethod)) {
            return List.of();
        }

        if (!context.supportsOrderMethod()) {
            return List.of();
        }

        if (context.businessHours().isEmpty()) {
            return List.of();
        }

        LocalDateTime now = context.now();
        ShopBusinessHour todayHour = shopOperatingStatusCalculator.selectApplicableHour(
            context.businessHours(), now.getDayOfWeek(), PUBLIC_HOLIDAY
        );
        if (todayHour == null || todayHour.isClosed()) {
            return List.of();
        }

        LocalDateTime earliest = earliestSlotStart(now, todayHour, orderMethod);
        LocalDateTime latest = latestSlotStart(now, todayHour);
        if (earliest.isAfter(latest)) {
            return List.of();
        }

        return collectOpenSlots(context, earliest, latest);
    }

    private LocalDateTime earliestSlotStart(LocalDateTime now, ShopBusinessHour todayHour, OrderMethod orderMethod) {
        LocalDateTime base = now;
        if (!todayHour.is24Hours() && todayHour.getOpenTime() != null) {
            LocalDateTime openAt = LocalDateTime.of(now.toLocalDate(), todayHour.getOpenTime());
            if (openAt.isAfter(base)) {
                base = openAt;
            }
        }
        return ceilToSlotUnit(base.plusMinutes(ScheduledOrderPolicy.leadTimeMinutes(orderMethod)));
    }

    private LocalDateTime latestSlotStart(LocalDateTime now, ShopBusinessHour todayHour) {
        if (todayHour.is24Hours()) {
            return now.plusHours(ScheduledOrderPolicy.MAX_HORIZON_HOURS_FOR_24H_SHOP);
        }

        LocalTime closeTime = todayHour.getCloseTime();
        LocalTime openTime = todayHour.getOpenTime();
        if (closeTime == null || openTime == null) {
            return now.plusHours(ScheduledOrderPolicy.MAX_HORIZON_HOURS_FOR_24H_SHOP);
        }

        LocalDate closeDate = closeTime.isAfter(openTime) ? now.toLocalDate() : now.toLocalDate().plusDays(1);
        return LocalDateTime.of(closeDate, closeTime).minusMinutes(ScheduledOrderPolicy.SLOT_UNIT_MINUTES);
    }

    private List<ScheduledOrderSlot> collectOpenSlots(
        ScheduledOrderSlotContext context,
        LocalDateTime earliest,
        LocalDateTime latest
    ) {
        List<ScheduledOrderSlot> slots = new ArrayList<>();
        boolean rangeSlot = ScheduledOrderPolicy.isRangeSlot(context.orderMethod());

        LocalDateTime candidate = earliest;
        for (int i = 0; i < MAX_SLOT_CANDIDATES && !candidate.isAfter(latest); i++) {
            if (isOpenAt(context, candidate)
                && (!rangeSlot || isOpenAt(context, candidate.plusMinutes(ScheduledOrderPolicy.SLOT_UNIT_MINUTES - 1)))) {
                slots.add(ScheduledOrderSlot.of(context.orderMethod(), candidate));
            }
            candidate = candidate.plusMinutes(ScheduledOrderPolicy.SLOT_UNIT_MINUTES);
        }
        return List.copyOf(slots);
    }

    private boolean isOpenAt(ScheduledOrderSlotContext context, LocalDateTime at) {
        return shopOperatingStatusCalculator.calculate(
            ShopOperatingStatusContext.of(
                context.shop(),
                context.businessHours(),
                context.breakTimes(),
                context.closedDays(),
                context.temporaryClosures(),
                context.suspensions(),
                context.orderMethod(),
                PUBLIC_HOLIDAY,
                at
            )
        ).isOpen();
    }

    private static LocalDateTime ceilToSlotUnit(LocalDateTime dateTime) {
        LocalDateTime truncated = dateTime.withSecond(0).withNano(0);
        int remainder = truncated.getMinute() % ScheduledOrderPolicy.SLOT_UNIT_MINUTES;
        if (remainder == 0) {
            return dateTime.equals(truncated)
                ? truncated
                : truncated.plusMinutes(ScheduledOrderPolicy.SLOT_UNIT_MINUTES);
        }
        return truncated.plusMinutes(ScheduledOrderPolicy.SLOT_UNIT_MINUTES - remainder);
    }
}

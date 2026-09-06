package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.shared.model.OrderMethod;

public final class ScheduledOrderPolicy {
    public static final int DELIVERY_LEAD_TIME_MINUTES = 120;

    public static final int TAKEOUT_LEAD_TIME_MINUTES = 60;

    public static final int SLOT_UNIT_MINUTES = 30;

    public static final int MAX_HORIZON_HOURS_FOR_24H_SHOP = 24;

    private ScheduledOrderPolicy() {
    }

    public static boolean supports(OrderMethod orderMethod) {
        return orderMethod == OrderMethod.DELIVERY || orderMethod == OrderMethod.TAKEOUT;
    }

    public static int leadTimeMinutes(OrderMethod orderMethod) {
        return switch (orderMethod) {
            case DELIVERY -> DELIVERY_LEAD_TIME_MINUTES;
            case TAKEOUT -> TAKEOUT_LEAD_TIME_MINUTES;
            default -> throw new IllegalArgumentException("예약주문을 지원하지 않는 주문 방법입니다: " + orderMethod);
        };
    }

    public static boolean isRangeSlot(OrderMethod orderMethod) {
        return orderMethod == OrderMethod.DELIVERY;
    }
}

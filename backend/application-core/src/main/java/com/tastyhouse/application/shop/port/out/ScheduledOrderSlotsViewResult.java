package com.tastyhouse.application.shop.port.out;

import java.util.List;

public record ScheduledOrderSlotsViewResult(
    boolean available,
    int leadTimeMinutes,
    int slotUnitMinutes,
    boolean rangeSlot,
    List<ScheduledOrderSlotItemResult> slots
) {
}

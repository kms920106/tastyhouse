package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shop.port.out.ScheduledOrderSlotsViewResult;

public interface ShopScheduledOrderSlotQueryUseCase {

    ScheduledOrderSlotsViewResult getScheduledOrderSlots(Long shopId, String orderMethod);
}

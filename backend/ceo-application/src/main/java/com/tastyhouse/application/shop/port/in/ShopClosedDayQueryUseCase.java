package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shop.port.out.ShopClosedDaysResult;

public interface ShopClosedDayQueryUseCase {

    ShopClosedDaysResult getClosedDays(Long ceoId, Long shopId);
}

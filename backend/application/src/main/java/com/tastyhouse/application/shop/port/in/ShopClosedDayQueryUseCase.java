package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import com.tastyhouse.application.shop.port.out.ShopClosedDaysResult;

@CeoApp
public interface ShopClosedDayQueryUseCase {

    ShopClosedDaysResult getClosedDays(Long ceoId, Long shopId);
}

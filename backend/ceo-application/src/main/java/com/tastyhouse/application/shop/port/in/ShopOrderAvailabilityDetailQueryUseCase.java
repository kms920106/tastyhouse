package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shop.port.out.ShopOrderAvailabilityViewResult;

public interface ShopOrderAvailabilityDetailQueryUseCase {

    ShopOrderAvailabilityViewResult getOrderAvailability(Long ceoId, Long shopId);
}

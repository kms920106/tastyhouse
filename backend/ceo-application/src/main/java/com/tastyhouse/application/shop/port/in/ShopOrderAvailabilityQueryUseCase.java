package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopOrderAvailabilityViewResult;
import com.tastyhouse.application.shop.port.out.ShopOrderMethodResult;

public interface ShopOrderAvailabilityQueryUseCase {

    ShopOrderAvailabilityViewResult getOrderAvailability(Long ceoId, Long shopId);

    List<ShopOrderMethodResult> getOrderMethods(Long ceoId, Long shopId);
}

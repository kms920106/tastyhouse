package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopOrderMethodResult;
import com.tastyhouse.application.shop.port.out.ShopOrderAvailabilityViewResult;

@CeoApp
public interface ShopOrderAvailabilityQueryUseCase {

    ShopOrderAvailabilityViewResult getOrderAvailability(Long ceoId, Long shopId);

    List<ShopOrderMethodResult> getOrderMethods(Long ceoId, Long shopId);
}

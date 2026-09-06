package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.WebApp;
import java.util.List;

import com.tastyhouse.application.shop.port.out.ScheduledOrderSlotsViewResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipViewResult;
import com.tastyhouse.application.shop.port.out.ShopOrderMethodItemResult;

@WebApp
public interface ShopOrderInfoQueryUseCase {

    ScheduledOrderSlotsViewResult getScheduledOrderSlots(Long shopId, String orderMethod);

    ShopDeliveryTipViewResult getShopDeliveryTip(Long shopId, Long memberId, Long deliveryAddressId, Integer orderAmount, String orderMethod);

    List<ShopOrderMethodItemResult> getShopOrderMethods(Long shopId);
}

package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shop.port.out.ShopDeliveryTipViewResult;

public interface ShopDeliveryTipViewQueryUseCase {

    ShopDeliveryTipViewResult getShopDeliveryTip(Long shopId, Long memberId, Long deliveryAddressId, Integer orderAmount, String orderMethod);
}

package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shop.port.out.ShopDeliveryTipOwnerViewResult;

public interface ShopDeliveryTipQueryUseCase {

    ShopDeliveryTipOwnerViewResult getDeliveryTips(Long ceoId, Long shopId);
}

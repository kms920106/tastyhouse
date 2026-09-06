package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipOwnerViewResult;

@CeoApp
public interface ShopDeliveryTipQueryUseCase {

    ShopDeliveryTipOwnerViewResult getDeliveryTips(Long ceoId, Long shopId);
}

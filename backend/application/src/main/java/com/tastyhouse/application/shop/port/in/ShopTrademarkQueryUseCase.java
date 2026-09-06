package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import com.tastyhouse.application.shop.port.out.ShopImageStatusResult;

@CeoApp
public interface ShopTrademarkQueryUseCase {

    ShopImageStatusResult getTrademarkStatus(Long ceoId, Long shopId);

    ShopImageStatusResult getThumbnailStatus(Long ceoId, Long shopId);
}

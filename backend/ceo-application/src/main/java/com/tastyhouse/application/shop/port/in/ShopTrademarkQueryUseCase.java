package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shop.port.out.ShopImageStatusResult;

public interface ShopTrademarkQueryUseCase {

    ShopImageStatusResult getTrademarkStatus(Long ceoId, Long shopId);

    ShopImageStatusResult getThumbnailStatus(Long ceoId, Long shopId);
}

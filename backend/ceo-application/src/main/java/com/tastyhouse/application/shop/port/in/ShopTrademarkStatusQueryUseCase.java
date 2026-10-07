package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shop.port.out.ShopImageStatusResult;

public interface ShopTrademarkStatusQueryUseCase {

    ShopImageStatusResult getTrademarkStatus(Long ceoId, Long shopId);
}

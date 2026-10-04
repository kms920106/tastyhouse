package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shop.port.out.ShopStatusResult;

public interface ShopStatusQueryUseCase {

    ShopStatusResult getStatus(Long ceoId, Long shopId);
}

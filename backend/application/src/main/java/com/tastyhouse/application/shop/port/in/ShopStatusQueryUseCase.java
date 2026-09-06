package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import com.tastyhouse.application.shop.port.out.ShopStatusResult;

@CeoApp
public interface ShopStatusQueryUseCase {

    ShopStatusResult getStatus(Long ceoId, Long shopId);
}

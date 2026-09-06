package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ShopScheduledOrderCommandUseCase {

    void updateScheduledOrder(ShopScheduledOrderUpdateCommand command);
}

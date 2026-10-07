package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaBulkResult;

public interface ShopDeliveryAreaBulkCreateUseCase {

    ShopDeliveryAreaBulkResult addDeliveryAreas(ShopDeliveryAreaBulkCreateCommand command);
}

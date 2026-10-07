package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaBulkDeleteResult;

public interface ShopDeliveryAreaBulkDeleteUseCase {

    ShopDeliveryAreaBulkDeleteResult removeDeliveryAreas(ShopDeliveryAreaBulkDeleteCommand command);
}

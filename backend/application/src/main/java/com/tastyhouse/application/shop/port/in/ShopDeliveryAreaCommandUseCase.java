package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaBulkDeleteResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaBulkResult;

@CeoApp
public interface ShopDeliveryAreaCommandUseCase {

    Long addDeliveryArea(ShopDeliveryAreaCreateCommand command);

    void removeDeliveryArea(ShopDeliveryAreaDeleteCommand command);

    ShopDeliveryAreaBulkResult addDeliveryAreas(ShopDeliveryAreaBulkCreateCommand command);

    ShopDeliveryAreaBulkDeleteResult removeDeliveryAreas(ShopDeliveryAreaBulkDeleteCommand command);

    ShopDeliveryAreaBulkResult applyRadius(ShopDeliveryAreaRadiusApplyCommand command);

    void savePolygon(ShopDeliveryAreaPolygonSaveCommand command);

    void deletePolygon(ShopDeliveryAreaPolygonDeleteCommand command);
}

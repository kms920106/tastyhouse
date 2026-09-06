package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ShopRiderGuideOwnerCommandUseCase {

    void updateVisitGuide(ShopRiderVisitGuideUpdateCommand command);

    void updatePickupLocation(ShopRiderPickupLocationOwnerUpdateCommand command);

    void clearPickupLocation(ShopRiderPickupLocationClearCommand command);
}

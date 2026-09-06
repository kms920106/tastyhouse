package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

@AdminApp
public interface ShopRiderGuideManagementCommandUseCase {

    void deleteVisitGuide(ShopRiderVisitGuideDeleteCommand command);

    Long requestRevision(ShopRiderVisitGuideRevisionCommand command);

    void updatePickupLocation(ShopRiderPickupLocationManagementUpdateCommand command);
}

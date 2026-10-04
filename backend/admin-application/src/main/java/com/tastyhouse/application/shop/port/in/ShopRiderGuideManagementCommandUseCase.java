package com.tastyhouse.application.shop.port.in;

public interface ShopRiderGuideManagementCommandUseCase {

    void deleteVisitGuide(ShopRiderVisitGuideDeleteCommand command);

    Long requestRevision(ShopRiderVisitGuideRevisionCommand command);

    void updatePickupLocation(ShopRiderPickupLocationManagementUpdateCommand command);
}

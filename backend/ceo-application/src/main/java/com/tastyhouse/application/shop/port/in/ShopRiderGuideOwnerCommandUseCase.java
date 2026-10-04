package com.tastyhouse.application.shop.port.in;

public interface ShopRiderGuideOwnerCommandUseCase {

    void updateVisitGuide(ShopRiderVisitGuideUpdateCommand command);

    void updatePickupLocation(ShopRiderPickupLocationOwnerUpdateCommand command);

    void clearPickupLocation(ShopRiderPickupLocationClearCommand command);
}

package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

@AdminApp
public interface ShopDeliveryAreaAdjustmentManagementCommandUseCase {

    void changeStatus(ShopDeliveryAreaAdjustmentStatusChangeCommand command);

    void rejectAdjustment(ShopDeliveryAreaAdjustmentRejectCommand command);
}

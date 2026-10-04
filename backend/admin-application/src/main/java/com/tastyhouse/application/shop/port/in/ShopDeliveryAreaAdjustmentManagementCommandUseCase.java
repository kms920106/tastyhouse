package com.tastyhouse.application.shop.port.in;

public interface ShopDeliveryAreaAdjustmentManagementCommandUseCase {

    void changeStatus(ShopDeliveryAreaAdjustmentStatusChangeCommand command);

    void rejectAdjustment(ShopDeliveryAreaAdjustmentRejectCommand command);
}

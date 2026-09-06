package com.tastyhouse.application.order.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

@AdminApp
public interface OrderManagementCommandUseCase {

    void changeStatus(OrderStatusChangeCommand command);

    void deleteOrder(OrderDeleteCommand command);
}

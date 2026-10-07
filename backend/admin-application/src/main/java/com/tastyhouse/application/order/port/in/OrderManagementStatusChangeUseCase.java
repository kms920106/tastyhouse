package com.tastyhouse.application.order.port.in;

public interface OrderManagementStatusChangeUseCase {

    void changeStatus(OrderStatusChangeCommand command);
}

package com.tastyhouse.application.order.port.in;

public interface OrderManagementCommandUseCase {

    void changeStatus(OrderStatusChangeCommand command);

    void deleteOrder(OrderDeleteCommand command);
}

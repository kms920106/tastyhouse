package com.tastyhouse.application.order.port.in;

public interface OrderCreateUseCase {

    Long createOrder(OrderCreateCommand command);
}

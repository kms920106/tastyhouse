package com.tastyhouse.application.order.port.in;

public interface OrderCommandUseCase {

    Long createOrder(OrderCreateCommand command);
}

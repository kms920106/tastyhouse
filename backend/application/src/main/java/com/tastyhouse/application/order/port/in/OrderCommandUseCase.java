package com.tastyhouse.application.order.port.in;

import com.tastyhouse.application.shared.marker.WebApp;

@WebApp
public interface OrderCommandUseCase {

    Long createOrder(OrderCreateCommand command);
}

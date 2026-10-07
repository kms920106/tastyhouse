package com.tastyhouse.application.order.port.in;

import com.tastyhouse.application.order.port.out.OrderDetailResult;

public interface OrderManagementDetailQueryUseCase {

    OrderDetailResult getOrder(Long id);
}

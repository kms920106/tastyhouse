package com.tastyhouse.application.order.port.in;

import com.tastyhouse.application.order.port.out.OrderDetailViewResult;

public interface OrderDetailQueryUseCase {

    OrderDetailViewResult getOrderDetail(Long memberId, Long orderId);
}

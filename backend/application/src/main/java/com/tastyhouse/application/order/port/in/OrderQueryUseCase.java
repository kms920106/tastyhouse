package com.tastyhouse.application.order.port.in;

import com.tastyhouse.application.order.port.out.OrderDetailViewResult;
import com.tastyhouse.application.order.port.out.OrderListItemResult;
import com.tastyhouse.application.shared.marker.WebApp;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@WebApp
public interface OrderQueryUseCase {

    PageResult<OrderListItemResult> getOrderList(Long memberId, int page, int size);

    OrderDetailViewResult getOrderDetail(Long memberId, Long orderId);
}

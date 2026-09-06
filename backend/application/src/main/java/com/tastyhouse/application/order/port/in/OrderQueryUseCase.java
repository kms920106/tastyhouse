package com.tastyhouse.application.order.port.in;

import com.tastyhouse.application.order.port.out.OrderListItemResult;
import com.tastyhouse.application.shared.marker.WebApp;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.order.port.out.OrderDetailViewResult;

@WebApp
public interface OrderQueryUseCase {

    PageResult<OrderListItemResult> getOrderList(Long memberId, int page, int size);

    OrderDetailViewResult getOrderDetail(Long memberId, Long orderId);
}

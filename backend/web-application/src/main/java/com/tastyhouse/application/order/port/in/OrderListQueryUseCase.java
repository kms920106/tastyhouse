package com.tastyhouse.application.order.port.in;

import com.tastyhouse.application.order.port.out.OrderListItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface OrderListQueryUseCase {

    PageResult<OrderListItemResult> getOrderList(Long memberId, int page, int size);
}

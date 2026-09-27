package com.tastyhouse.application.order.port.out;

import java.util.Optional;

import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface OrderManagementQueryPort {

    PageResult<OrderManagementListItemResult> findOrders(OrderSearchCondition condition, PageQuery pageQuery);

    Optional<OrderDetailResult> findOrderDetail(OrderId orderId);
}

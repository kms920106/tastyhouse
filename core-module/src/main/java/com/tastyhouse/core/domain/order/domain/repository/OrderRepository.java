package com.tastyhouse.core.domain.order.domain.repository;

import java.util.Optional;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.order.domain.model.Order;
import com.tastyhouse.core.domain.order.domain.vo.OrderId;
import com.tastyhouse.core.domain.order.application.dto.OrderSearchCondition;
import com.tastyhouse.core.domain.order.application.dto.result.OrderAdminListItemResult;
import com.tastyhouse.core.domain.order.application.dto.result.OrderListItemResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

public interface OrderRepository {

    Optional<Order> findById(OrderId orderId);

    PageResult<OrderListItemResult> findOrderListByMemberId(MemberId memberId, PageQuery pageQuery);

    PageResult<OrderAdminListItemResult> findOrders(OrderSearchCondition condition, PageQuery pageQuery);

    Order save(Order order);
}

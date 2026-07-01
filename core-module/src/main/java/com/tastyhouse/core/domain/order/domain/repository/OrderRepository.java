package com.tastyhouse.core.domain.order.domain.repository;

import com.tastyhouse.core.domain.order.application.dto.result.OrderListItemResult;
import com.tastyhouse.core.domain.order.domain.model.Order;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

import java.util.Optional;

public interface OrderRepository {

    Optional<Order> findById(Long orderId);

    PageResult<OrderListItemResult> findOrderListByMemberId(Long memberId, PageQuery pageQuery);

    Order save(Order order);
}

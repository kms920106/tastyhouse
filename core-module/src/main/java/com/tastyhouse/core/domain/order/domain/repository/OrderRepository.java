package com.tastyhouse.core.domain.order.domain.repository;

import com.tastyhouse.core.domain.order.application.dto.result.OrderListItemResult;
import com.tastyhouse.core.domain.order.domain.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface OrderRepository {

    Optional<Order> findById(Long orderId);

    Optional<Order> findByOrderNumber(String orderNumber);

    Page<OrderListItemResult> findOrderListByMemberId(Long memberId, Pageable pageable);

    boolean existsByOrderNumber(String orderNumber);

    Order save(Order order);
}

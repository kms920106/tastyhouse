package com.tastyhouse.core.repository.order;

import com.tastyhouse.core.entity.order.Order;
import com.tastyhouse.core.entity.order.OrderItem;
import com.tastyhouse.core.entity.order.OrderItemOption;
import com.tastyhouse.core.entity.order.OrderStatus;
import com.tastyhouse.core.entity.payment.dto.OrderListItemDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Optional<Order> findById(Long orderId);

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    Page<Order> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    List<Order> findByMemberIdAndOrderStatusOrderByCreatedAtDesc(Long memberId, OrderStatus orderStatus);

    List<Order> findByPlaceIdOrderByCreatedAtDesc(Long placeId);

    List<Order> findByPlaceIdAndOrderStatusOrderByCreatedAtDesc(Long placeId, OrderStatus orderStatus);

    boolean existsByOrderNumber(String orderNumber);

    Order save(Order order);

    Page<Order> findCompletedOrCancelledOrdersByMemberId(Long memberId, Pageable pageable);

    Page<OrderListItemDto> findOrderListByMemberId(Long memberId, Pageable pageable);

    Optional<OrderItem> findOrderItemById(Long orderItemId);

    List<OrderItem> findOrderItemsByOrderId(Long orderId);

    List<OrderItemOption> findOrderItemOptionsByOrderItemId(Long orderItemId);

    OrderItem saveOrderItem(OrderItem orderItem);

    void saveOrderItemOption(OrderItemOption orderItemOption);
}

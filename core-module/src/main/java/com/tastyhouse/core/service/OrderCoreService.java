package com.tastyhouse.core.service;

import com.tastyhouse.core.entity.order.Order;
import com.tastyhouse.core.entity.order.OrderItem;
import com.tastyhouse.core.entity.order.OrderItemOption;
import com.tastyhouse.core.entity.payment.Payment;
import com.tastyhouse.core.entity.payment.dto.OrderListItemDto;
import com.tastyhouse.core.repository.order.OrderRepository;
import com.tastyhouse.core.repository.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCoreService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public Optional<Order> findOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    @Transactional(readOnly = true)
    public Page<OrderListItemDto> findOrderListByMemberId(Long memberId, Pageable pageable) {
        return orderRepository.findOrderListByMemberId(memberId, pageable);
    }

    @Transactional(readOnly = true)
    public List<OrderItem> findOrderItemsByOrderId(Long orderId) {
        return orderRepository.findOrderItemsByOrderId(orderId);
    }

    @Transactional(readOnly = true)
    public List<OrderItemOption> findOrderItemOptionsByOrderItemId(Long orderItemId) {
        return orderRepository.findOrderItemOptionsByOrderItemId(orderItemId);
    }

    @Transactional(readOnly = true)
    public Optional<Payment> findPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    @Transactional
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    @Transactional
    public OrderItem saveOrderItem(OrderItem orderItem) {
        return orderRepository.saveOrderItem(orderItem);
    }

    @Transactional
    public void saveOrderItemOption(OrderItemOption orderItemOption) {
        orderRepository.saveOrderItemOption(orderItemOption);
    }
}

package com.tastyhouse.domain.order.service;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.model.Order;
import com.tastyhouse.domain.order.model.OrderStatus;
import com.tastyhouse.domain.order.repository.OrderRepository;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

public class OrderTransitionService {
    private final OrderRepository orderRepository;

    public OrderTransitionService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order load(OrderId orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND));
    }

    public Order loadOwnedBy(OrderId orderId, MemberId memberId) {
        Order order = load(orderId);
        order.validateOwnership(memberId);
        return order;
    }

    public Order loadOwnedBy(OrderId orderId, MemberId memberId, ErrorCode accessDeniedCode) {
        Order order = load(orderId);
        if (!order.getMemberId().equals(memberId)) {
            throw new BusinessException(accessDeniedCode);
        }
        return order;
    }

    public void changeStatus(OrderId orderId, OrderStatus status) {
        changeStatus(load(orderId), status);
    }

    public void changeStatus(Order order, OrderStatus status) {
        order.changeStatus(status);
        orderRepository.save(order);
    }

    public void confirm(Order order) {
        order.confirm();
        orderRepository.save(order);
    }

    public void cancel(Order order) {
        order.cancel();
        orderRepository.save(order);
    }

    public void delete(OrderId orderId) {
        Order order = load(orderId);
        order.delete();
        orderRepository.save(order);
    }
}

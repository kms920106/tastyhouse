package com.tastyhouse.application.order.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.model.Order;
import com.tastyhouse.domain.order.model.OrderStatus;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.application.order.port.out.write.OrderLoadPort;
import com.tastyhouse.application.order.port.out.write.OrderSavePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
public class OrderTransitionService {

    private final OrderLoadPort orderLoadPort;
    private final OrderSavePort orderSavePort;

    public OrderTransitionService(OrderLoadPort orderLoadPort, OrderSavePort orderSavePort) {
        this.orderLoadPort = orderLoadPort;
        this.orderSavePort = orderSavePort;
    }

    public Order load(OrderId orderId) {
        return orderLoadPort.findByIdIncludingDeleted(orderId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.ORDER_NOT_FOUND));
    }

    public Order loadOwnedBy(OrderId orderId, MemberId memberId) {
        Order order = load(orderId);
        order.validateOwnership(memberId);
        return order;
    }

    public Order loadOwnedBy(OrderId orderId, MemberId memberId, ApplicationErrorCode accessDeniedCode) {
        Order order = load(orderId);
        if (!order.getMemberId().equals(memberId)) {
            throw new ApplicationException(accessDeniedCode);
        }
        return order;
    }

    public void changeStatus(OrderId orderId, OrderStatus status) {
        changeStatus(load(orderId), status);
    }

    public void changeStatus(Order order, OrderStatus status) {
        order.changeStatus(status);
        orderSavePort.save(order);
    }

    public void confirm(Order order) {
        order.confirm();
        orderSavePort.save(order);
    }

    public void cancel(Order order) {
        order.cancel();
        orderSavePort.save(order);
    }

    public void delete(OrderId orderId) {
        Order order = load(orderId);
        order.delete();
        orderSavePort.save(order);
    }
}

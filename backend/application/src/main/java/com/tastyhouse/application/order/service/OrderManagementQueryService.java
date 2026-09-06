package com.tastyhouse.application.order.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.order.model.OrderStatus;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.payment.model.PaymentStatus;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.order.port.out.OrderDetailResult;
import com.tastyhouse.application.order.port.out.OrderManagementListItemResult;
import com.tastyhouse.application.order.port.out.OrderManagementQueryPort;
import com.tastyhouse.application.order.port.out.OrderSearchCondition;
import com.tastyhouse.application.order.port.in.OrderManagementQueryUseCase;

@Service
@AdminApp
@Transactional(readOnly = true)
public class OrderManagementQueryService implements OrderManagementQueryUseCase {

    private final OrderManagementQueryPort orderManagementQueryPort;

    public OrderManagementQueryService(OrderManagementQueryPort orderManagementQueryPort) {
        this.orderManagementQueryPort = orderManagementQueryPort;
    }

    @Override
    public PageResult<OrderManagementListItemResult> getOrders(
        Long shopId,
        String orderStatus,
        String orderMethod,
        String paymentStatus,
        String orderNumber,
        String ordererName,
        LocalDateTime startDate,
        LocalDateTime endDate,
        int page,
        int size
    ) {
        OrderSearchCondition condition = OrderSearchCondition.of(
            shopId,
            orderStatus == null ? null : OrderStatus.from(orderStatus),
            orderMethod == null ? null : OrderMethod.from(orderMethod),
            paymentStatus == null ? null : PaymentStatus.valueOf(paymentStatus),
            orderNumber,
            ordererName,
            startDate,
            endDate
        );
        PageQuery pageQuery = PageQuery.of(page, size);
        return orderManagementQueryPort.findOrders(condition, pageQuery);
    }

    @Override
    public OrderDetailResult getOrder(Long id) {
        return orderManagementQueryPort.findOrderDetail(OrderId.of(id))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND));
    }
}

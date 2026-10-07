package com.tastyhouse.application.order.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.order.model.OrderStatus;
import com.tastyhouse.domain.payment.model.PaymentStatus;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.application.order.port.in.OrderManagementListQueryUseCase;
import com.tastyhouse.application.order.port.out.OrderManagementListItemResult;
import com.tastyhouse.application.order.port.out.OrderManagementQueryPort;
import com.tastyhouse.application.order.port.out.OrderSearchCondition;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class OrderManagementListQueryService implements OrderManagementListQueryUseCase {

    private final OrderManagementQueryPort orderManagementQueryPort;

    public OrderManagementListQueryService(OrderManagementQueryPort orderManagementQueryPort) {
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
            orderStatus == null ? null : OrderStatus.from(orderStatus).name(),
            orderMethod == null ? null : OrderMethod.from(orderMethod).name(),
            paymentStatus == null ? null : PaymentStatus.valueOf(paymentStatus).name(),
            orderNumber,
            ordererName,
            startDate,
            endDate
        );
        PageQuery pageQuery = PageQuery.of(page, size);
        return orderManagementQueryPort.findOrders(condition, pageQuery);
    }
}

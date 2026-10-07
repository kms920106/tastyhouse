package com.tastyhouse.application.order.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.application.order.port.in.OrderManagementDetailQueryUseCase;
import com.tastyhouse.application.order.port.out.OrderDetailResult;
import com.tastyhouse.application.order.port.out.OrderManagementQueryPort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
class OrderManagementDetailQueryService implements OrderManagementDetailQueryUseCase {

    private final OrderManagementQueryPort orderManagementQueryPort;

    public OrderManagementDetailQueryService(OrderManagementQueryPort orderManagementQueryPort) {
        this.orderManagementQueryPort = orderManagementQueryPort;
    }

    @Override
    public OrderDetailResult getOrder(Long id) {
        return orderManagementQueryPort.findOrderDetail(OrderId.of(id).value())
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.ORDER_NOT_FOUND));
    }
}

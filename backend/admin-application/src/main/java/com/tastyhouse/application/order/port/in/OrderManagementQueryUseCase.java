package com.tastyhouse.application.order.port.in;

import java.time.LocalDateTime;

import com.tastyhouse.application.order.port.out.OrderDetailResult;
import com.tastyhouse.application.order.port.out.OrderManagementListItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface OrderManagementQueryUseCase {

    PageResult<OrderManagementListItemResult> getOrders(
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
    );

    OrderDetailResult getOrder(Long id);
}

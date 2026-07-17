package com.tastyhouse.adminapi.order;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.order.domain.model.OrderStatus;
import com.tastyhouse.core.domain.order.domain.vo.OrderId;
import com.tastyhouse.core.domain.payment.domain.model.PaymentStatus;
import com.tastyhouse.core.domain.shop.domain.model.OrderMethod;
import com.tastyhouse.core.domain.order.application.OrderCommandService;
import com.tastyhouse.core.domain.order.application.OrderQueryService;
import com.tastyhouse.core.domain.order.application.dto.OrderSearchCondition;
import com.tastyhouse.core.domain.order.application.dto.result.OrderResult;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.adminapi.order.response.OrderDetailResponse;
import com.tastyhouse.adminapi.order.response.OrderListItemResponse;
import com.tastyhouse.adminapi.order.response.OrderPageResponse;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderCommandService orderCommandService;
    private final OrderQueryService orderQueryService;

    public OrderPageResponse getOrders(Long shopId, String orderStatus, String orderMethod, String paymentStatus,
                                       String orderNumber, String ordererName,
                                       LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        OrderStatus status = orderStatus == null ? null : OrderStatus.from(orderStatus);
        OrderMethod method = orderMethod == null ? null : OrderMethod.from(orderMethod);
        PaymentStatus payment = paymentStatus == null ? null : PaymentStatus.valueOf(paymentStatus);
        OrderSearchCondition condition = OrderSearchCondition.of(shopId, status, method, payment, orderNumber, ordererName, startDate, endDate);
        PageResult<OrderListItemResponse> pageResult = orderQueryService.findOrders(condition, page, size)
            .map(OrderListItemResponse::from);
        return OrderPageResponse.from(pageResult);
    }

    public OrderDetailResponse getOrder(Long id) {
        OrderId orderId = OrderId.of(id);
        OrderResult result = orderQueryService.findOrderDetailById(orderId);
        return OrderDetailResponse.from(result);
    }

    public void changeStatus(Long id, String status) {
        OrderId orderId = OrderId.of(id);
        OrderStatus orderStatus = OrderStatus.from(status);
        orderCommandService.changeOrderStatus(orderId, orderStatus);
    }

    public void deleteOrder(Long id) {
        OrderId orderId = OrderId.of(id);
        orderCommandService.deleteOrder(orderId);
    }
}

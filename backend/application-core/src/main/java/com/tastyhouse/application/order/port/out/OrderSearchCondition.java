package com.tastyhouse.application.order.port.out;

import java.time.LocalDateTime;

public record OrderSearchCondition(
    Long shopId,
    String orderStatus,
    String orderMethod,
    String paymentStatus,
    String orderNumber,
    String ordererName,
    LocalDateTime startDate,
    LocalDateTime endDate
) {

    public static OrderSearchCondition of(
        Long shopId,
        String orderStatus,
        String orderMethod,
        String paymentStatus,
        String orderNumber,
        String ordererName,
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        return new OrderSearchCondition(
            shopId,
            orderStatus,
            orderMethod,
            paymentStatus,
            orderNumber,
            ordererName,
            startDate,
            endDate
        );
    }
}

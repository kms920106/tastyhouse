package com.tastyhouse.application.shop.port.out;

import java.util.List;

import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.OrderUnavailableReason;

public record ShopOrderAvailabilityViewResult(
    boolean orderable,
    OrderUnavailableReason unavailableReason,
    List<OrderMethodAvailability> orderMethods
) {

    public record OrderMethodAvailability(
        OrderMethod orderMethod,
        boolean orderable,
        OrderUnavailableReason unavailableReason
    ) {
    }
}

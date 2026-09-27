package com.tastyhouse.application.shop.port.out;

import java.util.List;

public record ShopOrderAvailabilityViewResult(
    boolean orderable,
    String unavailableReason,
    String unavailableReasonDisplayName,
    List<OrderMethodAvailability> orderMethods
) {

    public record OrderMethodAvailability(
        String orderMethod,
        String orderMethodDisplayName,
        boolean orderable,
        String unavailableReason,
        String unavailableReasonDisplayName
    ) {
    }
}

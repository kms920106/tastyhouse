package com.tastyhouse.application.order.port.out;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public record OrderListPaymentFilter(
    Collection<String> settledStatuses,
    String awaitingStatus,
    Collection<String> awaitingMethods
) {

    public OrderListPaymentFilter {
        settledStatuses = List.copyOf(settledStatuses);
        Objects.requireNonNull(awaitingStatus);
        awaitingMethods = List.copyOf(awaitingMethods);
    }

    public static OrderListPaymentFilter of(
        Collection<String> settledStatuses,
        String awaitingStatus,
        Collection<String> awaitingMethods
    ) {
        return new OrderListPaymentFilter(
            settledStatuses,
            awaitingStatus,
            awaitingMethods
        );
    }
}

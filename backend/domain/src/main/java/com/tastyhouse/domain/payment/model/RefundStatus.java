package com.tastyhouse.domain.payment.model;

import java.util.Set;

public enum RefundStatus {

    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED;

    public static Set<RefundStatus> limitOccupyingStatuses() {
        return LIMIT_OCCUPYING;
    }

    private static final Set<RefundStatus> LIMIT_OCCUPYING = Set.of(PENDING, PROCESSING, COMPLETED);
}

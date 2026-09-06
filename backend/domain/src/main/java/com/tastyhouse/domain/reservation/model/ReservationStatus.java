package com.tastyhouse.domain.reservation.model;

import java.util.Set;

public enum ReservationStatus {
    PENDING,
    CONFIRMED,
    REJECTED,
    CANCELED,
    COMPLETED;

    public boolean isBlocking() {
        return this == PENDING || this == CONFIRMED || this == COMPLETED;
    }

    public static Set<ReservationStatus> blockingStatuses() {
        return BLOCKING;
    }

    private static final Set<ReservationStatus> BLOCKING = Set.of(PENDING, CONFIRMED, COMPLETED);
}

package com.tastyhouse.application.payment.service;

import java.time.Duration;
import java.time.LocalDateTime;

final class PaymentReconcileBackoffPolicy {

    private static final Duration BASE_INTERVAL = Duration.ofMinutes(10);

    private static final Duration MAX_INTERVAL = Duration.ofHours(24);

    private static final Duration SCHEDULE_SLACK = Duration.ofMinutes(1);

    private static final Duration CLAIM_GUARD = Duration.ofMinutes(2);

    private static final int MAX_DOUBLINGS = 8;

    private PaymentReconcileBackoffPolicy() {
    }

    static LocalDateTime nextAttemptAt(int attemptsSoFar, LocalDateTime dueAt, LocalDateTime claimedAt) {
        Duration interval = attemptsSoFar >= MAX_DOUBLINGS
            ? MAX_INTERVAL
            : cappedAtMaxInterval(BASE_INTERVAL.multipliedBy(1L << attemptsSoFar));
        LocalDateTime onRunGrid = dueAt.plus(interval).minus(SCHEDULE_SLACK);
        LocalDateTime afterClaim = claimedAt.plus(CLAIM_GUARD);
        return onRunGrid.isAfter(afterClaim) ? onRunGrid : afterClaim;
    }

    private static Duration cappedAtMaxInterval(Duration interval) {
        return interval.compareTo(MAX_INTERVAL) <= 0 ? interval : MAX_INTERVAL;
    }
}

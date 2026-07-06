package com.tastyhouse.core.domain.reservation.domain.vo;

public record ReservationId(Long value) {

    public ReservationId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ReservationId는 양수여야 합니다: " + value);
        }
    }

    public static ReservationId of(Long value) {
        return new ReservationId(value);
    }
}

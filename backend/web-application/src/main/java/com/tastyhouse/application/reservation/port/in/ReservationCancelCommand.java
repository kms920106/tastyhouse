package com.tastyhouse.application.reservation.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ReservationCancelCommand(
    Long memberId,
    Long reservationId
) {

    public ReservationCancelCommand {
        if (memberId == null || reservationId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ReservationCancelCommand of(Long memberId, Long reservationId) {
        return new ReservationCancelCommand(memberId, reservationId);
    }
}

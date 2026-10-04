package com.tastyhouse.application.reservation.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ReservationRejectCommand(Long reservationId) {

    public ReservationRejectCommand {
        if (reservationId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ReservationRejectCommand of(Long reservationId) {
        return new ReservationRejectCommand(reservationId);
    }
}

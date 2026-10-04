package com.tastyhouse.application.reservation.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ReservationCompleteCommand(Long reservationId) {

    public ReservationCompleteCommand {
        if (reservationId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ReservationCompleteCommand of(Long reservationId) {
        return new ReservationCompleteCommand(reservationId);
    }
}

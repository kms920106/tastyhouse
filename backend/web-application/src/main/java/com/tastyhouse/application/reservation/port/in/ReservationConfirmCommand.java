package com.tastyhouse.application.reservation.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ReservationConfirmCommand(Long reservationId) {

    public ReservationConfirmCommand {
        if (reservationId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ReservationConfirmCommand of(Long reservationId) {
        return new ReservationConfirmCommand(reservationId);
    }
}

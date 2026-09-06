package com.tastyhouse.application.reservation.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ReservationCompleteCommand(Long reservationId) {
    public ReservationCompleteCommand {
        if (reservationId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ReservationCompleteCommand of(Long reservationId) {
        return new ReservationCompleteCommand(reservationId);
    }
}

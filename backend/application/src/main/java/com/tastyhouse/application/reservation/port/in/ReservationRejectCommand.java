package com.tastyhouse.application.reservation.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ReservationRejectCommand(Long reservationId) {
    public ReservationRejectCommand {
        if (reservationId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ReservationRejectCommand of(Long reservationId) {
        return new ReservationRejectCommand(reservationId);
    }
}

package com.tastyhouse.application.reservation.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ReservationConfirmCommand(Long reservationId) {
    public ReservationConfirmCommand {
        if (reservationId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ReservationConfirmCommand of(Long reservationId) {
        return new ReservationConfirmCommand(reservationId);
    }
}

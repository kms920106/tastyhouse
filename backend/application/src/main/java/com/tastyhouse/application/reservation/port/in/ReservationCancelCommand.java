package com.tastyhouse.application.reservation.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ReservationCancelCommand(
    Long memberId,
    Long reservationId
) {
    public ReservationCancelCommand {
        if (memberId == null || reservationId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ReservationCancelCommand of(Long memberId, Long reservationId) {
        return new ReservationCancelCommand(memberId, reservationId);
    }
}

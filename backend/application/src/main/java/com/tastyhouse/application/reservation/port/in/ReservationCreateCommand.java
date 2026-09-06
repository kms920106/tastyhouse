package com.tastyhouse.application.reservation.port.in;

import java.time.LocalDate;
import java.time.LocalTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ReservationCreateCommand(
    Long memberId,
    Long shopId,
    LocalDate reservationDate,
    LocalTime reservationTime,
    Integer partySize,
    String request,
    Boolean agreedRequiredTerms
) {
    public ReservationCreateCommand {
        if (memberId == null || shopId == null || reservationDate == null
            || reservationTime == null || partySize == null || agreedRequiredTerms == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

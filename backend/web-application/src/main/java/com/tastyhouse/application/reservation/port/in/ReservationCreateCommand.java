package com.tastyhouse.application.reservation.port.in;

import java.time.LocalDate;
import java.time.LocalTime;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

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
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}

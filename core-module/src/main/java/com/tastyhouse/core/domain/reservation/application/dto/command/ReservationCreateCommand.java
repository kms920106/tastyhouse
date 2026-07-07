package com.tastyhouse.core.domain.reservation.application.dto.command;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationCreateCommand(
    Long shopId,
    LocalDate date,
    LocalTime time,
    Integer partySize,
    String request,
    boolean agreedRequiredTerms
) {

    public static ReservationCreateCommand of(
        Long shopId,
        LocalDate date,
        LocalTime time,
        Integer partySize,
        String request,
        boolean agreedRequiredTerms
    ) {
        return new ReservationCreateCommand(shopId, date, time, partySize, request, agreedRequiredTerms);
    }
}

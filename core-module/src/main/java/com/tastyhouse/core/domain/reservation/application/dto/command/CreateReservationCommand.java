package com.tastyhouse.core.domain.reservation.application.dto.command;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreateReservationCommand(
    Long shopId,
    LocalDate date,
    LocalTime time,
    Integer partySize,
    String request,
    boolean agreedRequiredTerms
) {
}

package com.tastyhouse.application.reservation.port.in;

import com.tastyhouse.application.shared.marker.WebApp;

@WebApp
public interface ReservationCommandUseCase {

    Long createReservation(ReservationCreateCommand command);

    void confirmReservation(ReservationConfirmCommand command);

    void completeReservation(ReservationCompleteCommand command);

    void rejectReservation(ReservationRejectCommand command);

    void cancelReservation(ReservationCancelCommand command);
}

package com.tastyhouse.webapplication.reservation.port.in;

/**
 * 예약 쓰기 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ReservationCommandService})을 알지 않는다.
 */
public interface ReservationCommandUseCase {

    Long createReservation(ReservationCreateCommand command);

    void confirmReservation(ReservationConfirmCommand command);

    void completeReservation(ReservationCompleteCommand command);

    void rejectReservation(ReservationRejectCommand command);

    void cancelReservation(ReservationCancelCommand command);
}

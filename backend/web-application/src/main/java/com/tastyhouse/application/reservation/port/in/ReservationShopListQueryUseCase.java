package com.tastyhouse.application.reservation.port.in;

import java.util.List;

import com.tastyhouse.application.reservation.port.out.ReservationResult;

public interface ReservationShopListQueryUseCase {

    List<ReservationResult> getShopReservations(Long shopId);
}

package com.tastyhouse.application.reservation.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.reservation.port.in.ReservationShopListQueryUseCase;
import com.tastyhouse.application.reservation.port.out.ReservationQueryPort;
import com.tastyhouse.application.reservation.port.out.ReservationResult;

@Service
@Transactional(readOnly = true)
class ReservationShopListQueryService implements ReservationShopListQueryUseCase {

    private final ReservationQueryPort reservationQueryPort;

    public ReservationShopListQueryService(ReservationQueryPort reservationQueryPort) {
        this.reservationQueryPort = reservationQueryPort;
    }

    @Override
    public List<ReservationResult> getShopReservations(Long shopId) {
        return reservationQueryPort.findReservationsByShopId(shopId);
    }
}

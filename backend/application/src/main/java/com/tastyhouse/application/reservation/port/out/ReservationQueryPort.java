package com.tastyhouse.application.reservation.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.reservation.vo.ReservationId;

public interface ReservationQueryPort {

    List<ReservationResult> findReservationsByMemberId(Long memberId);

    List<ReservationResult> findReservationsByShopId(Long shopId);

    Optional<ReservationResult> findReservationById(ReservationId id);

    Optional<ReservationDetailResult> findReservationDetailById(ReservationId id);

    List<SlotOccupancyResult> findSlotOccupancies(Long shopId, LocalDate date);

    boolean existsBlockingReservation(Long memberId, Long shopId, LocalDate date);
}

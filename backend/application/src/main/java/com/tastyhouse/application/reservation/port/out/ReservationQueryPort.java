package com.tastyhouse.application.reservation.port.out;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReservationQueryPort {

    List<ReservationResult> findReservationsByMemberId(Long memberId);

    List<ReservationResult> findReservationsByShopId(Long shopId);

    Optional<ReservationResult> findReservationById(Long id);

    Optional<ReservationDetailResult> findReservationDetailById(Long id);

    List<SlotOccupancyResult> findSlotOccupancies(Long shopId, LocalDate date);

    boolean existsBlockingReservation(Long memberId, Long shopId, LocalDate date, Collection<String> blockingStatuses);
}

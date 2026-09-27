package com.tastyhouse.application.reservation.port.out.write;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

public interface ReservationStatePort {
    Optional<ReservationState> findById(Long id);

    boolean existsBlockingByMemberShopDate(Long memberId, Long shopId, LocalDate date, Collection<String> blockingStatuses);

    ReservationState save(ReservationState state);
}

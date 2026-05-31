package com.tastyhouse.core.domain.reservation.infrastructure.persistence;

import com.tastyhouse.core.domain.reservation.domain.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationJpaRepository extends JpaRepository<Reservation, Long> {
}

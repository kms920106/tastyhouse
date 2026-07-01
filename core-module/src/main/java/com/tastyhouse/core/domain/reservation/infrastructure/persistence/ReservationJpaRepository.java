package com.tastyhouse.core.domain.reservation.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.core.domain.reservation.domain.model.Reservation;

public interface ReservationJpaRepository extends JpaRepository<Reservation, Long> {
}

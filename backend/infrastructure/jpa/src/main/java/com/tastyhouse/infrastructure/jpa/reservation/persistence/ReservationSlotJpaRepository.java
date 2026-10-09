package com.tastyhouse.infrastructure.jpa.reservation.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface ReservationSlotJpaRepository extends JpaRepository<ReservationSlotJpaEntity, Long> {
}

package com.tastyhouse.infrastructure.persistence.reservation.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface ReservationSlotJpaRepository extends JpaRepository<ReservationSlotJpaEntity, Long> {
}

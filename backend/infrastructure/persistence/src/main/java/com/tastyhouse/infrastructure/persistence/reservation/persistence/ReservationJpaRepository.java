package com.tastyhouse.infrastructure.persistence.reservation.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface ReservationJpaRepository extends JpaRepository<ReservationJpaEntity, Long> {
}

package com.tastyhouse.infrastructure.jpa.holiday.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface PublicHolidayJpaRepository extends JpaRepository<PublicHolidayJpaEntity, Long> {
}

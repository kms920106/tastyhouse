package com.tastyhouse.infrastructure.persistence.holiday.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface PublicHolidayJpaRepository extends JpaRepository<PublicHolidayJpaEntity, Long> {
}

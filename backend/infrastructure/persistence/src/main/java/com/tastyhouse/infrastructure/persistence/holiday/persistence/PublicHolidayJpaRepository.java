package com.tastyhouse.infrastructure.persistence.holiday.persistence;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicHolidayJpaRepository extends JpaRepository<PublicHolidayJpaEntity, Long> {

    boolean existsByHolidayDate(LocalDate holidayDate);

    List<PublicHolidayJpaEntity> findAllByHolidayDateBetween(LocalDate from, LocalDate to);
}

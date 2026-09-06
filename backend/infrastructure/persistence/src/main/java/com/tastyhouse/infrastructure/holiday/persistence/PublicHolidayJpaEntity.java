package com.tastyhouse.infrastructure.holiday.persistence;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "PUBLIC_HOLIDAY")
public class PublicHolidayJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "is_substitute", nullable = false)
    private boolean substitute;

    protected PublicHolidayJpaEntity() {
    }

    public Long getId() {
        return this.id;
    }

    public LocalDate getHolidayDate() {
        return this.holidayDate;
    }

    public String getName() {
        return this.name;
    }
}

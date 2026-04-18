package com.tastyhouse.core.entity.place;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@Entity
@Table(name = "PLACE_BUSINESS_HOUR")
public class PlaceBusinessHour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private DayType dayType; // 요일 타입 (평일, 토요일, 일요일, 공휴일)

    @Column(name = "open_time")
    private LocalTime openTime; // 오픈 시간

    @Column(name = "close_time")
    private LocalTime closeTime; // 마감 시간

    @Column(name = "is_closed")
    private Boolean isClosed; // 해당 요일 휴무 여부
}

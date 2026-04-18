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
@Table(name = "PLACE_BREAK_TIME")
public class PlaceBreakTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private DayType dayType; // 요일 타입 (평일, 토요일, 일요일, 공휴일)

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime; // 브레이크타임 시작

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime; // 브레이크타임 종료
}

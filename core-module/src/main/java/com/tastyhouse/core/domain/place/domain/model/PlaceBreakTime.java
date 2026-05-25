package com.tastyhouse.core.domain.place.domain.model;

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
    private Long id; // PK

    @Column(name = "place_id", nullable = false)
    private Long placeId; // 장소 ID (PLACE.id 참조)

    @Enumerated(EnumType.STRING)
    @Column(name = "day_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private DayType dayType; // 요일 유형 (WEEKDAY, SATURDAY, SUNDAY, HOLIDAY 등)

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime; // 브레이크타임 시작 시각

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime; // 브레이크타임 종료 시각
}

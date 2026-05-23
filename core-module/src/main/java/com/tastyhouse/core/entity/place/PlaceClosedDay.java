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

@Getter
@Entity
@Table(name = "PLACE_CLOSED_DAY")
public class PlaceClosedDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "place_id", nullable = false)
    private Long placeId; // 장소 ID (PLACE.id 참조)

    @Enumerated(EnumType.STRING)
    @Column(name = "closed_day_type", nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private ClosedDayType closedDayType; // 정기 휴무 유형 (FIRST_MON, SECOND_SUN, EVERY_TUE 등)
}

package com.tastyhouse.core.domain.place.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Amenity {

    PARKING("주차"),
    RESTROOM("내부화장실"),
    RESERVATION("예약"),
    BABY_CHAIR("아기의자"),
    PET_FRIENDLY("애견동반"),
    OUTLET("개별 콘센트"),
    TAKEOUT("포장"),
    DELIVERY("배달");

    private final String displayName;
}

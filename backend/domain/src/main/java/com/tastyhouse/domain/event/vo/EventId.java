package com.tastyhouse.domain.event.vo;

public record EventId(Long value) {

    public EventId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("EventId는 양수여야 합니다: " + value);
        }
    }

    public static EventId of(Long value) {
        return new EventId(value);
    }
}

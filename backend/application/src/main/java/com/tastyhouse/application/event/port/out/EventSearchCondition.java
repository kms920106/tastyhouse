package com.tastyhouse.application.event.port.out;

import com.tastyhouse.domain.event.model.EventStatus;

public record EventSearchCondition(
    String name,
    EventStatus status
) {

    public static EventSearchCondition of(String name, EventStatus status) {
        return new EventSearchCondition(name, status);
    }
}

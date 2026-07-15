package com.tastyhouse.core.domain.event.application.dto;

import com.tastyhouse.core.domain.event.domain.model.EventStatus;

public record EventSearchCondition(
    String name,
    EventStatus status
) {

    public static EventSearchCondition of(String name, EventStatus status) {
        return new EventSearchCondition(name, status);
    }
}

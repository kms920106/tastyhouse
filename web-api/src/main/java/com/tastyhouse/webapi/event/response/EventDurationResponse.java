package com.tastyhouse.webapi.event.response;

import java.time.LocalDateTime;

public record EventDurationResponse(
    LocalDateTime startAt,
    LocalDateTime endAt
) {
    public static EventDurationResponse from(
        LocalDateTime startAt,
        LocalDateTime endAt
    ) {
        return new EventDurationResponse(
            startAt,
            endAt
        );
    }
}

package com.tastyhouse.application.event.port.out;

import java.time.LocalDateTime;

public record EventListItemResult(
    Long eventId,
    String name,
    String thumbnailUrl,
    LocalDateTime startAt,
    LocalDateTime endAt
) {
}

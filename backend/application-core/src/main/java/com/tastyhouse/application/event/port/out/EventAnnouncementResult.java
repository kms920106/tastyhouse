package com.tastyhouse.application.event.port.out;

import java.time.LocalDateTime;

public record EventAnnouncementResult(
    Long id,
    Long eventId,
    String name,
    String content,
    LocalDateTime announcedAt
) {
}
